package fi.aalto.cs.apluscourses.services

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.sun.net.httpserver.HttpServer
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.InetSocketAddress

@TestApplication
class HttpStatusMappingTest {
    private val projectFixture = projectFixture()

    @Test
    fun `public resource failures retain their status and URL`(): Unit = runBlocking {
        val courses = CoursesClient.getInstance(projectFixture.get())
        withResourceServer { baseUrl ->
            withTimeout(10_000) {
                for (status in listOf(401, 403, 400, 500)) {
                    val url = "$baseUrl/$status"
                    val failure = assertInstanceOf(
                        IOException::class.java,
                        runCatching { courses.get(url) }.exceptionOrNull()
                    )
                    assertTrue(failure.message.orEmpty().contains(status.toString()))
                    assertTrue(failure.message.orEmpty().contains(url))
                }
            }
        }
    }

    @Test
    fun `successful public resource responses pass`(): Unit = runBlocking {
        val courses = CoursesClient.getInstance(projectFixture.get())
        withResourceServer { baseUrl ->
            withTimeout(10_000) {
                for (status in listOf(HttpStatusCode.OK, HttpStatusCode.Created)) {
                    assertEquals(status, courses.get("$baseUrl/${status.value}").status)
                }
            }
        }
    }

    private suspend fun withResourceServer(block: suspend (String) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            exchange.use { it.sendResponseHeaders(it.requestURI.path.removePrefix("/").toInt(), -1) }
        }
        server.start()
        try {
            block("http://127.0.0.1:${server.address.port}")
        } finally {
            server.stop(0)
        }
    }
}
