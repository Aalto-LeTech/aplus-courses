package fi.aalto.cs.apluscourses.services

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.sun.net.httpserver.HttpServer
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList

@TestApplication
class ResourceRedirectTest {
    private val projectFixture = projectFixture()

    @Test
    fun `public resource requests follow redirects while the API transport does not`(): Unit = runBlocking {
        val tokens = CopyOnWriteArrayList<String?>()
        val bytes = "downloaded archive".encodeToByteArray()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            exchange.use {
                tokens += exchange.requestHeaders.getFirst("Authorization")
                if (exchange.requestURI.path == "/start") {
                    exchange.responseHeaders.add("Location", "/file")
                    exchange.sendResponseHeaders(302, -1)
                } else if (exchange.requestMethod == "HEAD") {
                    exchange.responseHeaders.add("Content-Length", bytes.size.toString())
                    exchange.sendResponseHeaders(200, -1)
                } else {
                    exchange.sendResponseHeaders(200, 0)
                    exchange.responseBody.write(bytes)
                }
            }
        }
        server.start()
        val target = Files.createTempFile("aplus-redirect", ".zip")
        try {
            val courses = CoursesClient.getInstance(projectFixture.get())
            val url = "http://127.0.0.1:${server.address.port}/start"
            withTimeout(10_000) {
                assertEquals(bytes.size.toLong(), courses.getFileSize(url))
                assertEquals(bytes.decodeToString(), courses.get(url).bodyAsText())
                courses.download(url, target.toFile())
                assertArrayEquals(bytes, Files.readAllBytes(target))
                Files.writeString(target, "old contents")
                courses.downloadFile(url, target)
                assertArrayEquals(bytes, Files.readAllBytes(target))
                assertEquals(HttpStatusCode.Found, courses.aplus.httpClient.get(url).status)
            }
            assertEquals(9, tokens.size)
            assertTrue(tokens.all { it == null }, "Public resource requests must carry no API token")
        } finally {
            server.stop(0)
            Files.deleteIfExists(target)
        }
    }
}
