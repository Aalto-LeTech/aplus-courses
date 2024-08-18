package fi.aalto.cs.apluscourses.utils.temp

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object FileDateFormatter {
    /**
     * Returns a string containing a formatted number of appropriate time units that have elapsed
     * since the file's last modification date. The string is formatted in the ways of
     * "6 seconds ago", "12 minutes ago" or "5 hours ago".
     *
     * @param filePath The file path which modification date is to be parsed.
     */
    @Throws(IOException::class)
    fun getFileModificationTime(filePath: Path): String {
        val fileTime = Files.getLastModifiedTime(filePath)
        val fileInstant = Instant.fromEpochMilliseconds(fileTime.toMillis())
        val currentTime = Clock.System.now()

        return DateDifferenceFormatter.formatWithLargestTimeUnit(fileInstant, currentTime, true)
    }
}
