package fi.aalto.cs.apluscourses.model.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class ScalaSdkTest {

    @Test
    fun `parses numeric version parts and ignores qualifiers`() {
        val cases = mapOf(
            "3.7.4" to Triple(3, 7, 4),
            RELEASE_CANDIDATE to Triple(3, 8, 0),
            "3.10.0" to Triple(3, 10, 0),
            "3" to Triple(3, 0, 0),
        )

        cases.forEach { (version, expected) ->
            assertEquals(expected, ScalaSdk.parseVersion(version), version)
        }
    }

    @Test
    fun `treats 3_8 and later as the new layout, comparing numerically not lexically`() {
        assertFalse(ScalaSdk.isScala38Plus("3.7.4"))
        assertTrue(ScalaSdk.isScala38Plus("3.8.0"))
        assertTrue(ScalaSdk.isScala38Plus(RELEASE_CANDIDATE))
        assertTrue(ScalaSdk.isScala38Plus("3.10.0"))
        assertTrue(ScalaSdk.isScala38Plus("4.0.0"))
    }

    @Test
    fun `finds the Scala 2 x standard library jar shipped alongside the SDK`(@TempDir dir: Path) {
        val lib = dir.resolve("lib")
        lib.createDirectories()
        lib.resolve("scala-library-2.13.14.jar").writeText("")
        lib.resolve("scala3-library_3-3.7.4.jar").writeText("")

        assertEquals("2.13.14", ScalaSdk.findStdlibUnder(lib))
    }

    @Test
    fun `ignores Scala 3 libraries when looking for the 2 x standard library`(@TempDir dir: Path) {
        val lib = dir.resolve("lib")
        lib.createDirectories()
        lib.resolve("scala3-library_3-3.7.4.jar").writeText("")

        assertNull(ScalaSdk.findStdlibUnder(lib))
    }

    @Test
    fun `ignores the sources jar downloaded next to the standard library`(@TempDir dir: Path) {
        val lib = dir.resolve("lib")
        lib.createDirectories()
        lib.resolve("scala-library-2.13.16.jar").writeText("")
        lib.resolve("scala-library-2.13.16-sources.jar").writeText("")
        lib.resolve("scala-library-2.13.16-javadoc.jar").writeText("")

        assertEquals("2.13.16", ScalaSdk.findStdlibUnder(lib))
    }

    @Test
    fun `returns null for a directory that does not exist`(@TempDir dir: Path) {
        assertNull(ScalaSdk.findStdlibUnder(dir.resolve("maven2")))
    }

    private companion object {
        @NonNls
        const val RELEASE_CANDIDATE = "3.8.0-RC1"
    }
}
