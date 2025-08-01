package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.serialization.PropertyMapping
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.utils.ZipUtil
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.jetbrains.annotations.NonNls
import java.io.File
import java.io.IOException
import java.nio.channels.WritableByteChannel
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.nameWithoutExtension

class UnauthorizedException @PropertyMapping() constructor() :
    IOException("The server responded with 401 Unauthorized") {
    private val serialVersionUID: Long = 1L
}

@OptIn(ExperimentalSerializationApi::class)
@Service(Service.Level.PROJECT)
class CoursesClient(
    private val project: Project,
    private val scope: CoroutineScope
) : AutoCloseable {

    @Volatile
    var client: HttpClient = buildClient()

    fun buildClient(
        protocol: URLProtocol = URLProtocol.HTTPS,
        host: String = "localhost",
        port: Int = 0,
        apiPath: String = "api/v2/"
    ): HttpClient =
        HttpClient(CIO) {
            install(Resources)
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    namingStrategy = JsonNamingStrategy.SnakeCase
                })
            }

            engine {
                endpoint {
                    maxConnectionsCount = 16
                }
                requestTimeout = 0
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

    suspend fun getFileSize(url: String): Long? =
        client.head(url).also(::verifyStatus).headers[HttpHeaders.ContentLength]?.toLongOrNull()

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
    }

    suspend fun download(url: String, file: File, withAuth: Boolean = false) {
        client.get(url) {
            if (withAuth) addToken()
        }.also(::verifyStatus)
            .bodyAsChannel()
            .copyAndClose(file.also { it.parentFile.mkdirs(); it.createNewFile() }.sinkChannel())
    }

    suspend fun downloadAndUnzip(
        zipUrl: String,
        target: Path,
        onlyPath: String? = null
    ) {
        withBackgroundProgress(project, MyBundle.message("aplusCourses")) {
            reportSequentialProgress { reporter ->
                val tempZip = createTempFile(target.nameWithoutExtension, ".zip").toFile()

                reporter.indeterminateStep(MyBundle.message("services.progress.downloading", zipUrl)) {
                    download(zipUrl, tempZip)
                }

                reporter.indeterminateStep(MyBundle.message("services.progress.extracting", zipUrl, target)) {
                    ZipUtil.unzip(tempZip, target.toFile(), onlyPath)
                    tempZip.delete()
                }
            }
        }
    }

    suspend fun fetchAndParse(url: String, regex: Regex, token: Boolean = false): String? {
        val body = get(url, token).bodyAsText()
        return regex.find(body)?.groupValues?.getOrNull(1)
    }

    suspend fun HttpRequestBuilder.addToken() {
        TokenStorage.getInstance().getToken()?.let {
            if (it.isNotBlank()) header(HttpHeaders.Authorization, "Token $it")
        }
    }

    fun verifyStatus(response: HttpResponse) {
        when (response.status) {
            HttpStatusCode.OK -> Unit
            HttpStatusCode.Unauthorized -> throw UnauthorizedException()
            else -> throw IOException("Unexpected ${response.status} for ${response.call.request.url}")
        }
    }

    suspend fun ByteReadChannel.copyAndClose(dest: WritableByteChannel): Unit =
        try {
            while (!isClosedForRead) {
                if (copyTo(dest, 8 * 1024) == 0L) break
            }
        } finally {
            dest.close()
        }

    private fun File.sinkChannel(): WritableByteChannel =
        outputStream().channel

    override fun close() {
        client.close()
    }

    companion object {
        fun getInstance(project: Project): CoursesClient = project.service()
    }
}