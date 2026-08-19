package fi.aalto.cs.apluscourses.services

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.IOException

@TestApplication
class HttpStatusMappingTest {
    private val projectFixture = projectFixture()

    private fun responseWith(status: HttpStatusCode): HttpResponse = runBlocking {
        HttpClient(MockEngine { respond("", status) }) { followRedirects = false }
            .use { it.get("https://example.org/") }
    }

    private fun verify(status: HttpStatusCode, redirectionIsSuccess: Boolean = false) =
        CoursesClient.getInstance(projectFixture.get())
            .verifyStatus(responseWith(status), redirectionIsSuccess)

    @Test
    fun `a 403 is the server saying this student is not on the course`() {
        assertThrows(ForbiddenException::class.java) { verify(HttpStatusCode.Forbidden) }
    }

    @Test
    fun `a 401 is a rejected token`() {
        assertThrows(UnauthorizedException::class.java) { verify(HttpStatusCode.Unauthorized) }
    }

    @Test
    fun `any other failure stays a plain IO error`() {
        val thrown = assertThrows(IOException::class.java) { verify(HttpStatusCode.InternalServerError) }

        assertFalse(thrown is ForbiddenException, "an outage is not a not-enrolled answer")
        assertFalse(thrown is UnauthorizedException, "an outage is not a rejected token")
    }

    @Test
    fun `a 200 passes`() {
        assertDoesNotThrow { verify(HttpStatusCode.OK) }
    }

    @Test
    fun `a 201 passes, because a submission POST answers with Created`() {
        assertDoesNotThrow { verify(HttpStatusCode.Created) }
    }

    @Test
    fun `a 400 on a submission is a failure, not a silent success`() {
        assertThrows(IOException::class.java) { verify(HttpStatusCode.BadRequest, redirectionIsSuccess = true) }
    }

    @Test
    fun `a redirect passes only where it is declared a success`() {
        assertDoesNotThrow { verify(HttpStatusCode.Found, redirectionIsSuccess = true) }
        assertThrows(IOException::class.java) { verify(HttpStatusCode.Found) }
    }
}
