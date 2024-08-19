package fi.aalto.cs.apluscourses.utils

import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.openapi.extensions.PluginId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = VersionSerializer::class)
open class Version(val major: Int, val minor: Int) {
    /**
     * @param versionString A version string of format `{major}.{minor}`.
     */
    constructor(versionString: String) : this(
        versionString.substringBefore(".").toInt(),
        versionString.substringAfter(".").toInt()
    )

    enum class ComparisonStatus {
        VALID,
        MINOR_TOO_OLD,
        MAJOR_TOO_OLD,
        MAJOR_TOO_NEW
    }

    operator fun compareTo(other: Version): Int {
        return if (this.major == other.major) {
            this.minor - other.minor
        } else {
            this.major - other.major
        }
    }

    /**
     * Compares a version with another version and returns the comparison result.
     */
    fun comparisonStatus(other: Version): ComparisonStatus {
        if (this.major < other.major) {
            return ComparisonStatus.MAJOR_TOO_OLD
        }
        if (this.major > other.major) {
            return ComparisonStatus.MAJOR_TOO_NEW
        }
        return if (this.minor >= other.minor) ComparisonStatus.VALID else ComparisonStatus.MINOR_TOO_OLD
    }

    override fun toString(): String {
        return "$major.$minor"
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Version && toString() == other.toString()
    }

    companion object {
        val EMPTY: Version = Version(0, 0)
        val DEFAULT: Version = Version(1, 0)
    }
}

class PluginVersion(major: Int, minor: Int, val patch: Int, val versionString: String) : Version(major, minor) {
    constructor(versionString: String) : this(
        versionString.substringBefore(".").toInt(),
        versionString.substringAfter(".").substringBefore(".").toInt(),
        versionString.substringAfterLast(".").substringBefore("-").toInt(),
        versionString
    )

    override fun toString(): String = versionString

    companion object {
        val current = getPlugin(PluginId.getId("fi.aalto.cs.intellij-plugin"))?.version ?: ""
    }
}

private object VersionSerializer : KSerializer<Version> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Version", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Version) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Version {
        return Version(decoder.decodeString())
    }
}
