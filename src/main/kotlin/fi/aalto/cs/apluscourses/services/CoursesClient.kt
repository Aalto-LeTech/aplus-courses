package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.application
import fi.aalto.cs.apluscourses.dal.TokenStorage
import fi.aalto.cs.apluscourses.utils.BuildInfo
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
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import java.nio.file.Files
import kotlin.io.path.Path

@OptIn(ExperimentalSerializationApi::class)
@Service(Service.Level.PROJECT)
class CoursesClient(
    val project: Project,
    val cs: CoroutineScope
) {
    private val userAgent: String
    var client: HttpClient // TODO private

    init {
        val appInfo = ApplicationInfo.getInstance()
        val appNamesInfo = ApplicationNamesInfo.getInstance()
        val intellijVersion = appInfo.fullVersion
        val product = appNamesInfo.productName // E.g. "IDEA"
        val edition = appNamesInfo.editionName // E.g. "Community Edition"
        val os = System.getProperty("os.name")
        val pluginVersion = BuildInfo.pluginVersion
//        val apacheUserAgent =
//            VersionInfo.getUserAgent("Apache-HttpClient", "org.apache.http.client", VersionInfo::class.java)

        userAgent = "intellij/$intellijVersion ($product; $edition; $os) apluscourses/$pluginVersion"

        this.client = HttpClient(CIO) {
            install(Resources)
            install(HttpCache) {
                val cacheFile =
                    Files.createDirectories(Path(project.basePath!!).resolve(".idea/aplusCourses/")).toFile()
                privateStorage(FileStorage(cacheFile))
            }
            install(UserAgent) {
                agent = userAgent
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
                    protocol = URLProtocol.HTTPS // TODO
                    host = "plus.cs.aalto.fi"
                    path("api/v2/")
//                    accept(ContentType.Application.Json)
//                    accept(ContentType.Text.CSV)
                    accept(ContentType.Any)
                }
            }
        }
    }

    fun updateAuthentication() {
        val token = TokenStorage.getToken()
        if (token == null) {
            println("Removing auth")
            client.config {
                headers {
                    remove("Authorization")
                }
            }
        } else {
            println("Setting auth")

            client = client.config {
                defaultRequest {
                    headers.appendIfNameAbsent("Authorization", "Token $token")
                }
            }
        }
    }

    suspend fun get(url: String): HttpResponse {
        return withContext(Dispatchers.IO) {
            client.get(url)
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

    @OptIn(ExperimentalSerializationApi::class)
    fun jsonb(snake: Boolean = true) = Json {
        ignoreUnknownKeys = true
        isLenient = true
        namingStrategy = if (snake) JsonNamingStrategy.SnakeCase else null
    }

    suspend inline fun <reified Resource : Any, reified Body : Any> getBody(resource: Resource): Body {
        val res = withContext(Dispatchers.IO) {
            client.get(resource, {
                headers {
                    append("Authorization", "Token ${TokenStorage.getToken()}")
                }
            })
        }
//        println(res.bodyAsText())
//        return json().decodeFromString<Body>(res.bodyAsText())
        return res.body<Body>()
    }

    suspend inline fun <reified Body : Any> getBody(url: String, snake: Boolean): Body {
        println(url)
        val res = withContext(Dispatchers.IO) {
            client.get(url)
        }
        return jsonb(snake).decodeFromString<Body>(res.bodyAsText())
    }

    suspend inline fun <reified Resource : Any> postForm(
        resource: Resource,
        form: List<PartData>
    ): HttpResponse {
        val res = withContext(Dispatchers.IO) {
            client.post(resource) {
                headers {
                    append("Authorization", "Token ${TokenStorage.getToken()}")
                }
                setBody(
                    MultiPartFormDataContent(
                        form,
//                        boundary = "APlusCoursesBoundary"
                    )
                )
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
