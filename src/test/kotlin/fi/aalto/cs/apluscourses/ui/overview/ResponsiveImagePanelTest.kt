package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.ui.scale.JBUIScale
import fi.aalto.cs.apluscourses.services.Background
import com.intellij.openapi.components.service
import com.intellij.testFramework.junit5.fixture.projectFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import kotlin.io.path.writeBytes
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

@TestApplication
class ResponsiveImagePanelTest {
    private val projectFixture = projectFixture()

    private fun banner(width: Int, maxHeight: Int) = ResponsiveImagePanel(
        width = width,
        maxWidth = width,
        maxHeight = maxHeight,
        scope = projectFixture.get().service<Background>().cs,
    )

    @Test
    fun `the banner keeps its aspect ratio while it fits`(): Unit = runBlocking(Dispatchers.EDT) {
        val panel = banner(width = 400, maxHeight = 1000)

        assertEquals(200, panel.height, "height should follow the aspect ratio")
    }

    @Test
    fun `a banner is downloaded to the cache only once`(@TempDir dir: Path): Unit =
        runBlocking(Dispatchers.EDT) {
            val source = dir.resolve("banner.png")
            source.writeBytes(onePixelPng())
            val cache = dir.resolve("cache")

            val first = ResponsiveImagePanel(
                width = 200,
                scope = projectFixture.get().service<Background>().cs,
                cacheDir = cache
            )
            first.setUrl(source.toUri().toString())
            waitUntil("the banner to be written to the cache") {
                Files.isDirectory(cache) && Files.list(cache).use { it.findAny().isPresent }
            }
            val cached = Files.list(cache).use { it.toList() }.single()
            val writtenAt = Files.getLastModifiedTime(cached)

            // The only copy left is the cached one, so a second fetch could not succeed even if
            // it were attempted, and any attempt would show up as the entry being rewritten.
            Files.delete(source)

            val second = ResponsiveImagePanel(
                width = 200,
                scope = projectFixture.get().service<Background>().cs,
                cacheDir = cache
            )
            second.setUrl(source.toUri().toString())
            waitUntil("the second panel to consult the cache") { !Files.exists(source) }

            assertTrue(
                Files.list(cache).use { it.count() } == 1L,
                "the banner should be cached under exactly one key"
            )
            assertEquals(
                writtenAt,
                Files.getLastModifiedTime(cached),
                "the second panel should have read the cached banner, not fetched it again"
            )
        }

    private fun onePixelPng(): ByteArray = Base64.getDecoder().decode(
        ONE_PIXEL_PNG_BASE64
    )

    private fun waitUntil(@NonNls what: String, condition: () -> Boolean) =
        PlatformTestUtil.waitWithEventsDispatching(TIMEOUT_PREFIX + what, condition, 10)

    @Test
    fun `an uncapped banner shows the whole image`() {
        assertEquals(0 to 400, visibleSlice(imageWidth = 800, imageHeight = 400, targetWidth = 400, targetHeight = 200))
    }

    @Test
    fun `a capped banner shows a centered band of the image`() {
        assertEquals(
            100 to 300,
            visibleSlice(imageWidth = 800, imageHeight = 400, targetWidth = 400, targetHeight = 100)
        )
    }

    @Test
    fun `the visible band never exceeds the image`() {
        val (top, bottom) = visibleSlice(imageWidth = 800, imageHeight = 400, targetWidth = 400, targetHeight = 9999)

        assertEquals(0, top)
        assertEquals(400, bottom)
    }

    @Test
    fun `a wide panel does not let the banner grow without limit`(): Unit = runBlocking(Dispatchers.EDT) {
        val maxHeight = 220
        val panel = banner(width = 2000, maxHeight = maxHeight)

        assertEquals(JBUIScale.scale(maxHeight), panel.height, "height should stop at the cap")
        assertTrue(panel.height < 2000 * HALF_HEIGHT_RATIO, "the uncapped height would have been far larger")
    }

    private companion object {
        const val HALF_HEIGHT_RATIO = 0.5

        @NonNls
        const val TIMEOUT_PREFIX = "timed out waiting for: "

        @NonNls
        const val ONE_PIXEL_PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    }
}
