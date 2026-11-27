package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.isActive
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class HttpClientDisposalTest {
    @Test
    fun `disposing the service parent closes the public resource client`() {
        val parent = Disposer.newDisposable()
        val service = IdeHttpClient()
        Disposer.register(parent, service)
        try {
            assertTrue(service.client.isActive)
        } finally {
            Disposer.dispose(parent)
        }
        assertFalse(service.client.isActive)
    }

    @Test
    fun `disposing the service parent closes the replacement API client`() {
        val parent = Disposer.newDisposable()
        val service = CoursesClient()
        Disposer.register(parent, service)
        val original = service.aplus.httpClient
        try {
            service.changeHost("https://plus.example.com")
            assertFalse(original.isActive)
            assertTrue(service.aplus.httpClient.isActive)
        } finally {
            Disposer.dispose(parent)
        }
        assertFalse(service.aplus.httpClient.isActive)
    }
}
