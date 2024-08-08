package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import org.jetbrains.annotations.NonNls
import java.nio.file.Files
import kotlin.io.path.Path

@OptIn(ExperimentalSerializationApi::class)
@Service(Service.Level.PROJECT)
class CoursesClient(
    val project: Project,
    val cs: CoroutineScope
) {
    val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(Resources)
            install(HttpCache) {
                val cacheFile =
                    Files.createDirectories(Path(project.basePath!!).resolve(".idea/aplusCourses/")).toFile()
                privateStorage(FileStorage(cacheFile))
            }
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

    suspend fun getFileSize(url: Url): Long? {
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

    companion object {
        fun getInstance(project: Project): CoursesClient {
            return project.service<CoursesClient>()
        }
    }
}
