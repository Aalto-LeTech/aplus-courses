package fi.aalto.cs.apluscourses.services

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class CoursesClientAbiTest {
    private val projectFixture = projectFixture()

    @Test
    fun `a request reaches the network layer rather than dying on a coroutines ABI mismatch`(): Unit = runBlocking {
        val client = CoursesClient.getInstance(projectFixture.get())
            .buildClient(protocol = URLProtocol.HTTP, host = "127.0.0.1", port = 1, apiPath = "")

        val failure = client.use {
            runCatching { it.get("http://127.0.0.1:1/") }.exceptionOrNull()
        }

        assertNotNull(failure, "expected the request to a closed port to fail")
        assertTrue(
            failure !is LinkageError && failure?.cause !is LinkageError,
            "ktor and the bundled kotlinx-coroutines disagree on the ABI: $failure"
        )
    }
}
