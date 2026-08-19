package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.serialization.PropertyMapping
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.apluscourses.utils.ZipUtil
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
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

class UnauthorizedException @PropertyMapping() constructor() :
    IOException("The server responded with 401 Unauthorized") {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

class ForbiddenException @PropertyMapping() constructor() :
    IOException("The server responded with 403 Forbidden") {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

typealias DownloadProgress = (copiedBytes: Long, totalBytes: Long) -> Unit

@Service(Service.Level.PROJECT)
class CoursesClient(
    private val scope: CoroutineScope
) : AutoCloseable {

    @Volatile
    var client: HttpClient = buildClient()

    /** Opted in for [JsonNamingStrategy.SnakeCase], which the A+ API's field names need. */
    @OptIn(ExperimentalSerializationApi::class)
    fun buildClient(
        protocol: URLProtocol = URLProtocol.HTTPS,
        @NonNls host: String = "localhost",
        port: Int = 0,
        @NonNls apiPath: String = "api/v2/"
    ): HttpClient =
        HttpClient(Java) {
            install(Resources)
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    namingStrategy = JsonNamingStrategy.SnakeCase
                })
            }

            defaultRequest {
                url {
                    this.protocol = protocol
                    this.host = host
                    this.port = port
                    path(apiPath)
                }
            }
        }

    fun changeHost(@NonNls newHost: String) {
        val protocol = if (newHost.startsWith("https", true)) URLProtocol.HTTPS else URLProtocol.HTTP
        val stripped = newHost.removePrefix("https://").removePrefix("http://")
        val hostPart = stripped.substringBefore('/')
        val host = hostPart.substringBefore(':')
        val port = hostPart.substringAfter(':', "").toIntOrNull() ?: 0
        client.close()
        client = buildClient(protocol, host, port)
    }

    fun execute(block: suspend (CoursesClient) -> Unit): Job =
        scope.launch { block(this@CoursesClient) }

    /** Sizes of files already asked about. */
    private val fileSizes = ConcurrentHashMap<String, Long>()

    /**
     * The download size of [url], from a cached value when possible.
     */
    suspend fun getFileSize(url: String): Long? {
        fileSizes[url]?.let { return it }

        val size = client.prepareHead(url).execute { response ->
            verifyStatus(response)
            response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        }
        size?.let { fileSizes[url] = it }
        return size
    }

    suspend fun get(
        url: String,
        withAuth: Boolean = false,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse = client.get(url) {
        if (withAuth) addToken()
        builder()
    }.also(::verifyStatus)

    suspend inline fun <reified Resource : Any> get(
        resource: Resource,
        crossinline builder: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse = client.get(resource) {
        addToken()
        builder()
    }.also(::verifyStatus).body()

    suspend inline fun <reified Resource : Any, reified R : Any> getBody(
        resource: Resource,
        crossinline builder: HttpRequestBuilder.() -> Unit = {}
    ): R = client.get(resource) {
        addToken()
        builder()
    }.also(::verifyStatus).body()

    suspend inline fun <reified Resource : Any> postForm(
        resource: Resource,
        parts: List<PartData>
    ): HttpResponse = client.post(resource) {
        addToken()
        setBody(MultiPartFormDataContent(parts))
    }.also { verifyStatus(it, redirectionIsSuccess = true) }

    /**
     * Streams [url] into [file], reporting how far along it is.
     */
    suspend fun download(
        url: String,
        file: File,
        withAuth: Boolean = false,
        onProgress: DownloadProgress? = null,
    ) {
        val target = file.also { it.parentFile.mkdirs(); it.createNewFile() }

        client.prepareGet(url) {
            if (withAuth) addToken()
        }.execute { response ->
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

    suspend fun downloadFile(url: String, target: Path) {
        reportSequentialProgress { reporter ->
            val tempFile = createTempFile(target.nameWithoutExtension).toFile()

            reporter.nextStep(endFraction = 100) {
                download(url, tempFile)
                Files.move(
                    tempFile.toPath(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
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

    suspend fun HttpRequestBuilder.addToken() {
        TokenStorage.getInstance().getToken()?.let {
            @NonNls val token = "Token $it"
            if (it.isNotBlank()) header(HttpHeaders.Authorization, token)
        }
    }

    /**
     * A POST is answered with 201 Created, and the client does not follow redirects for POSTs,
     * so [redirectionIsSuccess] lets the submission call treat a 3xx as the success it is.
     */
    fun verifyStatus(response: HttpResponse, redirectionIsSuccess: Boolean = false) {
        when {
            response.status.isSuccess() -> Unit
            redirectionIsSuccess && response.status.value in 300..399 -> Unit
            response.status == HttpStatusCode.Unauthorized -> throw UnauthorizedException()
            response.status == HttpStatusCode.Forbidden -> throw ForbiddenException()
            else -> throw IOException("Unexpected ${response.status} for ${response.call.request.url}")
        }
    }

    suspend fun ByteReadChannel.copyAndClose(
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

    override fun close() {
        client.close()
    }

    companion object {
        private const val COPY_CHUNK_BYTES = 8L * 1024

        private const val DOWNLOAD_END_FRACTION = 95

        private const val MIN_PROGRESS_INTERVAL_MS = 250L

        fun getInstance(project: Project): CoursesClient = project.service()
    }
}