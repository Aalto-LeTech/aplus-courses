package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.serialization.PropertyMapping
import fi.aalto.cs.apluscourses.MyBundle.message
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
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.jetbrains.annotations.NonNls
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.readBytes
import kotlin.sequences.forEach
import kotlin.text.contains

class UnauthorizedException @PropertyMapping() constructor() : Exception() {
    private val serialVersionUID: Long = 1L
}

@OptIn(ExperimentalSerializationApi::class)
@Service(Service.Level.PROJECT)
class CoursesClient(
    val project: Project,
    val cs: CoroutineScope
) {
    var client: HttpClient =
        HttpClient(CIO)


    fun changeHost(newHost: String) {
        @NonNls val https = "https"
        @NonNls val apiPath = "api/v2/"
        val protocol =
            if (newHost.substringBefore("://").lowercase() == https) URLProtocol.HTTPS else URLProtocol.HTTP
        val host = newHost.substringAfter("://").substringBeforeLast("/").substringBeforeLast(":")
        val port = newHost.substringAfterLast(":").substringBefore("/").toIntOrNull() ?: 0

        client = HttpClient(CIO) {
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
                    maxConnectionsCount = 8
                }
                requestTimeout = 0
            }
            defaultRequest {
                url {
                    this@url.protocol = protocol
                    this@url.host = host
                    this@url.port = port
                    path(apiPath)
                }
            }
        }
    }

    fun execute(action: suspend (CoursesClient) -> Unit) {
        cs.launch {
            action(this@CoursesClient)
        }
    }

    suspend fun HttpRequestBuilder.addToken() {
        @NonNls val key = "Authorization"
        @NonNls val value = "Token ${TokenStorage.getInstance().getToken()}"
        header(key, value)
    }

    suspend fun get(url: String, token: Boolean = false): HttpResponse {
        val res = withContext(Dispatchers.IO) {
            client.get(url) {
                if (token) {
                    addToken()
                }
            }
        }
        if (res.status != HttpStatusCode.OK) {
            throw IOException("Failed to get URL [$url]: ${res.status}")
        }
        return res
    }

    suspend fun getFileSize(url: String): Long? {
        val head = withContext(Dispatchers.IO) {
            client.request(url) {
                method = HttpMethod.Head
            }
        }
        return head.contentLength()
    }

    suspend inline fun <reified Resource : Any> get(
        resource: Resource,
        crossinline requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        val res = withContext(Dispatchers.IO) {
            client.get(resource) {
                addToken()
                requestBuilder()
            }
        }
        if (res.status != HttpStatusCode.OK) {
            throw IOException("Failed to get resource: ${res.status}")
        }
        return res
    }

    suspend inline fun <reified Resource : Any, reified Body : Any> getBody(
        resource: Resource,
        crossinline requestBuilder: HttpRequestBuilder.() -> Unit = {}
    ): Body {
        val res = withContext(Dispatchers.IO) {
            client.get(resource) {
                addToken()
                requestBuilder()
            }
        }
        if (res.status == HttpStatusCode.Unauthorized) {
            throw UnauthorizedException()
        }
        if (res.status != HttpStatusCode.OK) {
            throw IOException("Failed to get body: ${res.status}")
        }
        return res.body<Body>()
    }

    suspend inline fun <reified Resource : Any> postForm(
        resource: Resource,
        form: List<PartData>
    ): HttpResponse {
        val res = withContext(Dispatchers.IO) {
            client.post(resource) {
                addToken()
                setBody(MultiPartFormDataContent(form))
            }
        }
        return res
    }

    suspend fun fetch(url: String, file: File) {
        val response = get(url)
        if (response.status != HttpStatusCode.OK) {
            throw IOException("Failed to get file: ${response.status}")
        }
        val bodyChannel = response.bodyAsChannel()
        file.outputStream().use { fileOutputStream ->
            runBlocking {
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                while (bodyChannel.readAvailable(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    suspend fun getAndUnzip(zipUrl: String, target: Path, onlyPath: String? = null) {
        withBackgroundProgress(project, message("aplusCourses")) {
            reportSequentialProgress { reporter ->
                val tempZipFile = kotlin.io.path.createTempFile(target.nameWithoutExtension, ".zip").toFile()
                reporter.indeterminateStep(message("services.progress.downloading", zipUrl)) {
                    fetch(zipUrl, tempZipFile)
                }
                reporter.indeterminateStep(message("services.progress.extracting", zipUrl, target)) {
                    val destination = target
                    val destinationFile = destination.toFile()

                    withContext(Dispatchers.IO) {
                        if (!destinationFile.exists()) {
                            destinationFile.mkdirs()
                        }

                        ZipFile(tempZipFile).use { zip ->
                            zip.entries().asSequence().forEach { entry ->
                                zip.getInputStream(entry).use { inputStream ->
                                    val file = destination.resolve(entry.name).toFile()
                                    if (onlyPath != null && !file.path.contains(onlyPath)) {
                                        return@use
                                    }
                                    if (entry.isDirectory) {
                                        file.mkdir()
                                    } else {
                                        if (!file.parentFile.exists()) {
                                            file.parentFile.mkdirs()
                                        }
                                        file.writeBytes(inputStream.readBytes())
                                    }
                                }
                            }
                        }
                        tempZipFile.delete()
                    }
                }
            }
        }
    }

    companion object {
        fun getInstance(project: Project): CoursesClient {
            return project.service<CoursesClient>()
        }
    }
}
