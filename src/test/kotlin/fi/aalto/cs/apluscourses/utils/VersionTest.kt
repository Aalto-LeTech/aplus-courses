package fi.aalto.cs.apluscourses.utils

import fi.aalto.cs.aplus.courseconfig.ConfigVersion
import fi.aalto.cs.apluscourses.utils.PluginVersion.ComparisonStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VersionTest {
    @Test
    fun `comparisonStatus reports how the plugin version relates to the required one`() {
        val required = ConfigVersion(4, 2)
        assertEquals(ComparisonStatus.VALID, PluginVersion("4.2.0").comparisonStatus(required))
        assertEquals(ComparisonStatus.VALID, PluginVersion("4.3.1").comparisonStatus(required))
        assertEquals(ComparisonStatus.MINOR_TOO_OLD, PluginVersion("4.1.9").comparisonStatus(required))
        assertEquals(ComparisonStatus.MAJOR_TOO_OLD, PluginVersion("3.9.0").comparisonStatus(required))
        assertEquals(ComparisonStatus.MAJOR_TOO_NEW, PluginVersion("5.0.0").comparisonStatus(required))
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
