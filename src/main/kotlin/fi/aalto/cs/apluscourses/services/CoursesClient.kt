package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.components.Service
import fi.aalto.cs.apluscourses.dal.TokenStorage
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService.Exercises
import fi.aalto.cs.apluscourses.utils.BuildInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.scalastyle.scalariform.VisitorHelper.Clazz
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

/**
 * A utility class with methods for getting resources from a remote. For most use cases,
 * the [CoursesClient.fetch] methods are sufficient. The [CoursesClient.fetchAndMap]
 * and [CoursesClient.fetchAndConsume] methods can be used when direct access to
 * the input stream of the response is needed.
 */
@Service(
    Service.Level.APP
)
class CoursesClient(
//    private val project: Project,
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
        val pluginVersion = BuildInfo.INSTANCE.pluginVersion.toString()
//        val apacheUserAgent =
//            VersionInfo.getUserAgent("Apache-HttpClient", "org.apache.http.client", VersionInfo::class.java)

        userAgent = "intellij/$intellijVersion ($product; $edition; $os) apluscourses/$pluginVersion"

        this.client = HttpClient(CIO) {
            install(Resources)
            install(HttpCache) {
                val cacheFile = Files.createDirectories(Paths.get(".idea/aplusCourses/.http-cache")).toFile()
                privateStorage(FileStorage(cacheFile))
            }
            install(UserAgent) {
                agent = userAgent
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


//    private var httpClient: CloseableHttpClient? = null

//    /**
//     * Makes a GET request to the given URL with the given authentication in the request and returns
//     * the response body in a [ByteArrayInputStream].
//     *
//     * @param url            The URL to which the request is made.
//     * @param authentication The authentication that gets added to the request.
//     * @return A [ByteArrayInputStream] containing the response body.
//     * @throws IOException If an error (e.g. network error) occurs while downloading the file. This
//     * is an instance of [InvalidAuthenticationException] if the response
//     * status code is 401 or 403.
//     */
    /**
     * Makes a GET request to the given URL and returns the response body in a
     * [ByteArrayInputStream].
     *
     * @param url The URL to which the request is made.
     * @return A [ByteArrayInputStream] containing the response body.
     * @throws IOException If an error (e.g. network error) occurs while downloading the file. This is
     * an instance of [UnexpectedResponseException] if the status code of
     * the response isn't 2xx or the response is missing a body.
     */
    @Throws(IOException::class)
    suspend fun fetch(
        url: URL,
    ): ByteArrayInputStream =
        withContext(Dispatchers.IO) {
            val response = client.get(url.toString())
            ByteArrayInputStream(response.body())
        }
//        return fetchAndMap(
//            url, authentication
//        ) { response: HttpResponse ->
//            requireResponseEntity(response)
//            ByteArrayInputStream(EntityUtils.toByteArray(response.entity))
//        }

    suspend fun get(url: String): HttpResponse {
        return withContext(Dispatchers.IO) {
            client.get(url)
        }
    }

    suspend fun getFileSize(url: URL): Long? {
        val head = withContext(Dispatchers.IO) {
            client.request(url) {
                method = HttpMethod.Head
            }
        }
        return head.contentLength()
    }

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    suspend inline fun <reified Resource : Any, reified Body : Any> getBody(resource: Resource): Body {
        val res = withContext(Dispatchers.IO) {
            client.get(resource)
        }
        return json.decodeFromString<Body>(res.bodyAsText())
    }

    /**
     * Downloads a file from the given URL into the given file.
     *
     * @throws IOException If an error (e.g. network error) occurs while downloading
     * the file. This is an instance of [                     ] if the status code of the
     * response isn't 2xx or the response is missing a body.
     */
    @Throws(IOException::class)
    suspend fun fetch(
        url: URL,
        file: File,
    ) {
        withContext(Dispatchers.IO) {
            val response = client.get(url.toString())
            file.writeBytes(response.body())
        }
//        fetchAndConsume(
//            url
//        ) { response: HttpResponse ->
////            requireResponseEntity(response)
//            withContext(Dispatchers.IO) {
//                file.writeBytes(response.body())
//            }
//        }
    }

    /**
     * Makes a GET request to the given URL and returns the mapped response.
     *
     * @param url            The URL to which the GET request is made.
     * @param mapper         A [ResponseMapper] that converts the [HttpResponse] instance
     * to the desired format.
     * @return The result of `mapper.map(response)`, where response is a [HttpResponse]
     * containing the response.
     * @throws IOException If an issue occurs while making the request, which includes cases such as
     * an unknown host. This is an instance of [UnexpectedResponseException]
     * if the status code isn't 2xx.
     */
    @Throws(IOException::class)
    suspend fun <T> fetchAndMap(
        url: URL,
        mapper: ResponseMapper<T>
    ): T {
        val request = client.get(url.toString())
        return mapResponse(request, mapper)
    }

    /**
     * Makes a GET request to the given URL and consumes the response.
     *
     * @param url            The URL to which the GET request is made.
     * @param consumer       A [ResponseConsumer] that consumes the [HttpResponse]
     * instance.
     * @throws IOException If an issue occurs while making the request, which includes cases such as
     * an unknown host. This is an instance of [UnexpectedResponseException]
     * if the status code isn't 2xx.
     */
    @Throws(IOException::class)
    suspend fun fetchAndConsume(
        url: URL,
        consumer: ResponseConsumer
    ) {
        val response = withContext(Dispatchers.IO) {
            client.get(url.toString())
        }
        consumeResponse(response, consumer)
    }

    /**
     * Sends a POST request to the given URL and returns the value created by the mapper.
     *
     * @param url            A URL.
     * @param data           Map of request data.  Values can be strings, numbers or files.
     * @param mapper         A [ResponseMapper] that maps the HTTP response to the desired
     * format.
     * @return The value created by passing the response to the given mapper.
     * @throws IOException In case of I/O related errors or non-successful response.
     */
    @Throws(IOException::class)
    suspend fun <T> post(
        url: URL,
        data: Map<String, Any>?,
        mapper: ResponseMapper<T>
    ): T? {
        val builder = ParametersBuilder()
//        val builder = MultipartEntityBuilder.create()
        if (data != null) {
            for ((key, value) in data) {
                builder.append(key, value.toString())
//                builder.addPart(key, getContentBody(value))
            }
        }
        val response = client.post(url.toString()) {
            setBody(builder.build())
        }
//        request.entity = builder.build()

//        authentication?.addToRequest(request)

        return mapResponse(response, mapper)
    }

//    private fun getContentBody(value: Any): ContentBody {
//        if (value is String) {
//            return StringBody(value, ContentType.MULTIPART_FORM_DATA)
//        }
//        if (value is Number) {
//            return getContentBody(value.toString())
//        }
//        if (value is File) {
//            return FileBody(value)
//        }
//        throw IllegalArgumentException("Type of value not supported.")
//    }

    /**
     * Executes the given request, performs some checks on the response and returns the result of
     * passing the response to the given mapper.
     */
    @Throws(IOException::class)
    private fun <T> mapResponse(
        response: HttpResponse,
        mapper: ResponseMapper<T>
    ): T {
        requireSuccessStatusCode(response)
        return mapper.map(response)
    }

    /**
     * Executes the given request, performs some checks on the response and passes the response to the
     * given consumer.
     */
    @Throws(IOException::class)
    private fun consumeResponse(
        response: HttpResponse,
        consumer: ResponseConsumer
    ) {
        requireSuccessStatusCode(response)
        consumer.consume(response)
    }

    /**
     * Throws a [UnexpectedResponseException] if the response status code isn't 2xx. If the
     * response body includes JSON with an array for the key "errors" or a string for the key
     * "detail", then those messages are included in the exception message.
     */
    @Throws(IOException::class)
    fun requireSuccessStatusCode(response: HttpResponse) {
        val statusCode = response.status.value
        if (statusCode >= 200 && statusCode < 300) {
            return
        }

//        var details: String?
//        try {
//            val entity = response.entity
//            if (entity == null) {
//                details = "" + statusCode + " " + response.status.description
//            } else {
//                // Two possibilities: {"errors": [...]} and {"detail": "..."} NOSONAR
//                val json = JSONObject(
//                    JSONTokener(
//                        ByteArrayInputStream(EntityUtils.toByteArray(entity))
//                    )
//                )
//                details = json.optString("detail")
//                if (details == null || details.trim { it <= ' ' }.isEmpty()) {
//                    details = json
//                        .getJSONArray("errors")
//                        .toList()
//                        .stream()
//                        .map { obj: Any -> obj.toString() }
//                        .collect(Collectors.joining(", "))
//                }
//            }
//        } catch (e: JSONException) {
//            details = "" + statusCode + " " + response.status.description
//        }
//        throw UnexpectedResponseException(response, details!!)
    }

//    /**
//     * Throws a [UnexpectedResponseException] if the response entity is null.
//     */
//    @Throws(UnexpectedResponseException::class)
//    private fun requireResponseEntity(response: HttpResponse) {
//        if (response.b == null) {
//            throw UnexpectedResponseException(response, "Response is missing body")
//        }
//    }

    /**
     * A functional interface for adding authentication to an HTTP request.
     */
    fun interface HttpAuthentication {
        fun addToRequest(request: HttpRequest?)
    }

    /**
     * A functional interface for functions that map a [HttpResponse] to a desired result.
    //     * See [EntityUtils] for useful methods for working with [HttpEntity] instances.
     */
    fun interface ResponseMapper<T> {
        @Throws(IOException::class)
        fun map(response: HttpResponse): T
    }

    /**
     * A functional interface for functions that consume a [HttpResponse] and use it for
     * side-effects.
     */
    fun interface ResponseConsumer {
        @Throws(IOException::class)
        fun consume(response: HttpResponse)
    }
}
