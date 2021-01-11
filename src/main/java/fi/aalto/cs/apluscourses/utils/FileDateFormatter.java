package fi.aalto.cs.apluscourses.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class FileDateFormatter {
    private static String formatWithLargestTimeUnit(Instant i) {
        Instant currentTime = Instant.now();
        return ChronoUnit.SECONDS.between(i, currentTime) + " seconds ago";
    }

    /**
     * Returns a string containing a formatted number of appropriate time units that have elapsed since the file's
     * last modification date. The string is formatted in the ways of "6 seconds ago", "12 minutes ago" or "5 hours ago".
     * @param filePath The file path which modification date is to be parsed.
     */
    public static String getFileModificationTime(Path filePath) throws IOException {
        FileTime fileTime = Files.getLastModifiedTime(filePath);

        return formatWithLargestTimeUnit(fileTime.toInstant());
    }

    private FileDateFormatter() {

    }
}
