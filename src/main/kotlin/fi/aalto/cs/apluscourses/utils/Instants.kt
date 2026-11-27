package fi.aalto.cs.apluscourses.utils

import org.jetbrains.annotations.NonNls
import kotlin.time.Instant

/**
 * Parses an ISO-8601 timestamp and logs malformed input.
 * Malformed timestamps return null so callers can treat them as absent.
 */
fun parseInstantOrNull(@NonNls text: String): Instant? = try {
    Instant.parse(text)
} catch (e: IllegalArgumentException) {
    CoursesLogger.warn("Not a usable timestamp: \"$text\"", e)
    null
}

/** Logs malformed timestamps at debug level for callers that parse on every recompute. */
fun parseInstantOrNullQuietly(@NonNls text: String): Instant? = try {
    Instant.parse(text)
} catch (_: IllegalArgumentException) {
    CoursesLogger.debug("Not a usable timestamp: \"$text\"")
    null
}
