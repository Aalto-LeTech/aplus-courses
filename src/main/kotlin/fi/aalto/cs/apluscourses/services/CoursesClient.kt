package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.reportSequentialProgress
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.apluscourses.utils.ZipUtil
import fi.aalto.cs.aplus.client.AplusClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.io.IOException
import java.nio.channels.WritableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createTempFile
import kotlin.io.path.nameWithoutExtension

typealias DownloadProgress = (copiedBytes: Long, totalBytes: Long) -> Unit

@Service(Service.Level.PROJECT)
class CoursesClient : Disposable {

    /** Authenticated API transport with the IDE's trusted certificates. */
    @Volatile
    var aplus: AplusClient = buildAplusClient("https://localhost")
        private set

    private fun buildAplusClient(@NonNls baseUrl: String): AplusClient =
        AplusClient.create(
            baseUrl = baseUrl,
            tokenProvider = { TokenStorage.getInstance().getToken(baseUrl)?.toString() },
        ) {
            engine { config { trustIdeCertificates().allowConcurrentRequests() } }
        }

    /**
     * Replaces the API client after validating [newHost].
     * Invalid URLs leave the existing client open.
     *
     * @throws IllegalArgumentException when [newHost] is not a usable A+ URL.
     */
    fun changeHost(@NonNls newHost: String) {
        val aplusBaseUrl = AplusClient.normalizeBaseUrl(newHost)
        val replacement = buildAplusClient(aplusBaseUrl)
        val previous = aplus
        aplus = replacement
        previous.close()
    }

    /** Public resources follow redirects and never carry API credentials. */
    private val resourceClient get() = IdeHttpClient.getInstance().client

    private val fileSizes = ConcurrentHashMap<String, Long>()

    /** Returns the Content-Length in bytes, caching successful lookups by URL. */
    suspend fun getFileSize(url: String): Long? {
        fileSizes[url]?.let { return it }

        val size = resourceClient.prepareHead(url).execute { response ->
            verifyStatus(response)
            response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        }
        size?.let { fileSizes[url] = it }
        return size
    }

    suspend fun get(url: String): HttpResponse = resourceClient.get(url).also(::verifyStatus)

    /** Streams a download to [file]. Progress callbacks require a positive Content-Length. */
    suspend fun download(
        url: String,
        file: File,
        onProgress: DownloadProgress? = null,
    ): Unit = withContext(Dispatchers.IO) {
        val target = file.also { it.parentFile.mkdirs(); it.createNewFile() }

        resourceClient.prepareGet(url).execute { response ->
            verifyStatus(response)

            val totalBytes = response.contentLength()?.takeIf { it > 0 }
            if (totalBytes == null) {
                CoursesLogger.warn("No Content-Length for $url, downloading without progress")
                response.bodyAsChannel().copyAndClose(target.sinkChannel())
                return@execute
            }

            reportSequentialProgress { reporter ->
                var lastPercent = 0
                var lastReportedAt = 0L
                response.bodyAsChannel().copyAndClose(target.sinkChannel()) { copiedBytes ->
                    val percent = (copiedBytes * 100 / totalBytes).toInt().coerceIn(0, 100)
                    val now = System.currentTimeMillis()
                    val lastCall = percent == 100 && lastPercent < 100
                    if (!lastCall && now - lastReportedAt < MIN_PROGRESS_INTERVAL_MS) {
                        return@copyAndClose
                    }
                    lastReportedAt = now
                    if (percent > lastPercent) {
                        lastPercent = percent
                        reporter.nextStep(percent)
                    }
                    onProgress?.invoke(copiedBytes, totalBytes)
                }
            }
        }
    }

    suspend fun downloadFile(url: String, target: Path): Unit = withContext(Dispatchers.IO) {
        val tempFile = createTempFile(target.nameWithoutExtension).toFile()
        try {
            download(url, tempFile)
            Files.move(tempFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(tempFile.toPath())
        }
    }

    suspend fun downloadAndUnzip(
        zipUrl: String,
        target: Path,
        onlyPath: String? = null,
        onProgress: DownloadProgress? = null,
    ) {
        reportSequentialProgress { reporter ->
            val tempZip = createTempFile(target.nameWithoutExtension, ".zip").toFile()
            try {
                reporter.nextStep(endFraction = DOWNLOAD_END_FRACTION) {
                    download(zipUrl, tempZip, onProgress = onProgress)
                }

                reporter.nextStep(endFraction = 100) {
                    withContext(Dispatchers.IO) {
                        ZipUtil.unzip(tempZip, target.toFile(), onlyPath) { ensureActive() }
                    }
                }
            } finally {
                tempZip.delete()
            }
        }
    }

    private fun verifyStatus(response: HttpResponse) {
        if (!response.status.isSuccess()) {
            throw IOException("Unexpected ${response.status} for ${response.call.request.url}")
        }
    }

    private suspend fun ByteReadChannel.copyAndClose(
        dest: WritableByteChannel,
        onBytesCopied: (Long) -> Unit = {}
    ) {
        val source = this
        withContext(Dispatchers.IO) {
            dest.use { channel ->
                var copied = 0L
                while (!source.isClosedForRead) {
                    ensureActive()
                    val chunk = source.copyTo(channel, COPY_CHUNK_BYTES)
                    if (chunk == 0L) break
                    copied += chunk
                    onBytesCopied(copied)
                }
            }
        }
    }

    private fun File.sinkChannel(): WritableByteChannel =
        outputStream().channel

    override fun dispose() {
        aplus.close()
    }

    companion object {
        private const val COPY_CHUNK_BYTES = 8L * 1024

        private const val DOWNLOAD_END_FRACTION = 95

        private const val MIN_PROGRESS_INTERVAL_MS = 250L

        fun getInstance(project: Project): CoursesClient = project.service()
    }
}
