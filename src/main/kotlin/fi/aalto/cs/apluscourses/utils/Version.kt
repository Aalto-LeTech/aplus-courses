package fi.aalto.cs.apluscourses.utils

import kotlinx.serialization.Serializable

@Serializable
data class Version(val major: Int, val minor: Int) {

    init {
        require(!(major < 0 || minor < 0)) { "All the parts of version number must be non-negative." }
    }

    class InvalidVersionStringException(versionString: String, cause: Throwable?) : RuntimeException(
        "Version string '$versionString' does not match the expected pattern.", cause
    )

    enum class ComparisonStatus {
        VALID,
        MINOR_TOO_OLD,
        MAJOR_TOO_OLD,
        MAJOR_TOO_NEW
    }


    /**
     * Compares a version with another version and returns the comparison result.
     */
    fun compareTo(other: Version): ComparisonStatus {
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
        @JvmField
        val EMPTY: Version = Version(0, 0)

        /**
         * Returns a [Version] object based on the value of the given string.
         *
         * @param versionString A version string of format "{major}.{minor}.{build}".
         * @return A [Version] object.
         * @throws InvalidVersionStringException If the given string is invalid.
         */
        @JvmStatic
        fun fromString(versionString: String): Version {
            val major: Int
            val minor: Int

            val parts = versionString.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            //    if (parts.length != 2) {
//      throw new InvalidVersionStringException(versionString, null);
//    } TODO
            try {
                major = parts[0].toInt()
                minor = parts[1].toInt()
                return Version(major, minor)
            } catch (ex: NumberFormatException) {
                throw InvalidVersionStringException(versionString, ex)
            }
        }
    }
}
