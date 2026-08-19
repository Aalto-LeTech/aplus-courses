package fi.aalto.cs.apluscourses.utils

import fi.aalto.cs.apluscourses.utils.Version.ComparisonStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VersionTest {
    @Test
    fun `parses major and minor from a version string`() {
        val version = Version("4.12")
        assertEquals(4, version.major)
        assertEquals(12, version.minor)
    }

    @Test
    fun `comparisonStatus reports how a version relates to the required one`() {
        assertEquals(ComparisonStatus.VALID, Version("4.2").comparisonStatus(Version("4.2")))
        assertEquals(ComparisonStatus.VALID, Version("4.3").comparisonStatus(Version("4.2")))
        assertEquals(ComparisonStatus.MINOR_TOO_OLD, Version("4.1").comparisonStatus(Version("4.2")))
        assertEquals(ComparisonStatus.MAJOR_TOO_OLD, Version("3.9").comparisonStatus(Version("4.0")))
        assertEquals(ComparisonStatus.MAJOR_TOO_NEW, Version("5.0").comparisonStatus(Version("4.9")))
    }

    @Test
    fun `equality and toString round-trip through the version string`() {
        assertEquals(Version("2.7"), Version(2, 7))
        assertEquals("2.7", Version(2, 7).toString())
    }

    @Test
    fun `the plugin version is packaged as a resource and parses`() {
        assertTrue(PluginVersion.current.isNotBlank(), "version resource missing from the plugin")
        assertTrue(
            PluginVersion.current.matches(Regex("""\d+\.\d+\.\d+.*""")),
            "unexpected version string: ${PluginVersion.current}"
        )
        assertEquals(PluginVersion.current, PluginVersion.currentVersion.toString())
    }
}
