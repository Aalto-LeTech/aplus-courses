package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
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
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.jetbrains.annotations.NonNls
import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.readBytes
import kotlin.sequences.forEach
import kotlin.text.contains

@OptIn(ExperimentalSerializationApi::class)
@Service(Service.Level.PROJECT)
class CoursesClient(
    val project: Project,
    val cs: CoroutineScope
) {
    val client: HttpClient by lazy {
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
                    maxConnectionsCount = 8
                }
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "plus.cs.aalto.fi"
                    path("api/v2/")
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
        return withContext(Dispatchers.IO) {
            client.get(url) {
                if (token) {
                    addToken()
                }
            }
        }
    }

    suspend fun getFileSize(url: String): Long? {
        val head = withContext(Dispatchers.IO) {
            client.request(url) {
                method = HttpMethod.Head
            }
        }
        return head.contentLength()
    }

    suspend inline fun <reified Resource : Any, reified Body : Any> getBody(resource: Resource): Body {
        val res = withContext(Dispatchers.IO) {
            client.get(resource) {
                addToken()
            }
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
                onUpload { bytesSentTotal, contentLength ->
                    println("Sent $bytesSentTotal bytes from $contentLength")
                }
            }
        }
        println(res.headers.toString())
        println(res)
        println(res.bodyAsText())
        return res
    }

    suspend fun getAndUnzip(zipUrl: String, target: Path, onlyPath: String? = null) {
        println("Downloading and unzipping $zipUrl to $target")
        withBackgroundProgress(project, "A+ Courses") {
            reportSequentialProgress { reporter ->
                val tempZipFile = kotlin.io.path.createTempFile(target.nameWithoutExtension, ".zip").toFile()
                reporter.indeterminateStep("Downloading $zipUrl") {
                    val response = get(zipUrl)
                    if (response.status != HttpStatusCode.OK) {
                        throw IOException("Failed to get file: ${response.status}")
                    }
                    println("Downloading $zipUrl to $target")
                    val bodyChannel = response.bodyAsChannel()
                    tempZipFile.outputStream().use { fileOutputStream ->
                        runBlocking {
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            while (bodyChannel.readAvailable(buffer).also { bytesRead = it } != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    println("Downloaded $zipUrl to $tempZipFile")
                }
                reporter.indeterminateStep("Extracting $zipUrl to $target") {
                    val destination = target
                    val destinationFile = destination.toFile()

                    withContext(Dispatchers.IO) {
                        if (!destinationFile.exists()) {
                            destinationFile.mkdirs()
                        }
                        println("$tempZipFile ${tempZipFile.exists()} ${tempZipFile.length()} ${tempZipFile.canRead()}")

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
        println("Extracted $zipUrl to $target")
    }

    companion object {
        fun getInstance(project: Project): CoursesClient {
            return project.service<CoursesClient>()
        }
    }
}
