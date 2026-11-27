package fi.aalto.cs.apluscourses.utils

import fi.aalto.cs.aplus.courseconfig.ConfigVersion
import org.jetbrains.annotations.NonNls
import java.util.Properties

/**
 * Uses the first two version components for course compatibility checks.
 * [toString] preserves the full installed version.
 */
class PluginVersion(val version: ConfigVersion, private val versionString: String) {
    constructor(versionString: String) : this(
        ConfigVersion.parse(versionString.split('.').take(2).joinToString(".")),
        versionString
    )

    enum class ComparisonStatus {
        VALID,
        MINOR_TOO_OLD,
        MAJOR_TOO_OLD,
        MAJOR_TOO_NEW
    }

    /** Classifies major and minor mismatches for course compatibility notifications. */
    fun comparisonStatus(required: ConfigVersion): ComparisonStatus = when {
        version.major < required.major -> ComparisonStatus.MAJOR_TOO_OLD
        version.major > required.major -> ComparisonStatus.MAJOR_TOO_NEW
        version.minor >= required.minor -> ComparisonStatus.VALID
        else -> ComparisonStatus.MINOR_TOO_OLD
    }

    override fun toString(): String = versionString

    companion object {
        @NonNls
        private const val VERSION_RESOURCE = "/aplus-courses-version.properties"

        /**
         * Written by the `generatePluginVersionResource` Gradle task from the `pluginVersion`
         * property.
         */
        @NonNls
        val current: String = PluginVersion::class.java.getResourceAsStream(VERSION_RESOURCE)
            ?.use { stream -> Properties().apply { load(stream) }.getProperty("version") }
            ?: ""

        val currentVersion: PluginVersion =
            runCatching { PluginVersion(current) }.getOrElse {
                CoursesLogger.error("Could not read the plugin version from $VERSION_RESOURCE", it)
                PluginVersion(ConfigVersion(0, 0), current)
            }
    }
}
