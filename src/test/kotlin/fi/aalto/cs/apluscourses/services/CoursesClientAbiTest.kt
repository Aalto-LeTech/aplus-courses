package fi.aalto.cs.apluscourses.services

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import io.ktor.client.request.get
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class CoursesClientAbiTest {
    private val projectFixture = projectFixture()

    @Test
    fun `a request reaches the network layer rather than dying on a coroutines ABI mismatch`(): Unit = runBlocking {
        val client = CoursesClient.getInstance(projectFixture.get()).aplus.httpClient

        val failure = runCatching { client.get("http://127.0.0.1:1/") }.exceptionOrNull()

        assertNotNull(failure, "expected the request to a closed port to fail")
        assertTrue(
            failure !is LinkageError && failure?.cause !is LinkageError,
            "ktor and the bundled kotlinx-coroutines disagree on the ABI: $failure"
        )
    }

    @Test
    fun `an unusable course URL is rejected before the working clients are closed`() {
        val courses = CoursesClient.getInstance(projectFixture.get())
        val before = courses.aplus

        assertThrows(IllegalArgumentException::class.java) { courses.changeHost("  ") }

        assertSame(before, courses.aplus, "the previous client must survive a bad URL")
        assertTrue(before.httpClient.isActive, "the previous client must not have been closed")
    }

    @Test
    fun `a usable course URL replaces the client`() {
        val courses = CoursesClient.getInstance(projectFixture.get())
        val before = courses.aplus

        courses.changeHost("https://plus.example.com")

        assertNotSame(before, courses.aplus)
        assertEquals("https://plus.example.com/api/v2", courses.aplus.baseUrl)
    }
}
