package fi.aalto.cs.apluscourses.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FileDateFormatter {
    // the order of time units to check, ordered from most to least precise
    private static final List<ChronoUnit> TimeUnits = Arrays.asList(
        ChronoUnit.SECONDS,
        ChronoUnit.MINUTES,
        ChronoUnit.HOURS,
        ChronoUnit.DAYS,
        ChronoUnit.MONTHS,
        ChronoUnit.YEARS
    );

    private static String formatWithTimeUnit(ZonedDateTime fileTime, ZonedDateTime currentTime, ChronoUnit timeUnit) {
        return timeUnit.between(fileTime, currentTime) + " " + timeUnit.toString().toLowerCase(Locale.ROOT) + " ago";
    }

    private static String formatWithLargestTimeUnit(ZonedDateTime fileTime) {
        ZonedDateTime currentTime = ZonedDateTime.now(Clock.system(ZoneOffset.UTC));

        if (ChronoUnit.SECONDS.between(fileTime, currentTime) <= 0) {
            return "just now";
        }

        for (int i = 1; i < TimeUnits.size(); i++) {
            if (TimeUnits.get(i).between(fileTime, currentTime) == 0) {
                return formatWithTimeUnit(fileTime, currentTime, TimeUnits.get(i - 1));
            }
        }

        return formatWithTimeUnit(fileTime, currentTime, TimeUnits.get(TimeUnits.size() - 1));
    }

    /**
     * Same as {@link FileDateFormatter#getFileModificationTime(Path, Clock)} with the clock
     * being always set to the system's current time.
     *
     * @see FileDateFormatter#getFileModificationTime(Path, Clock)
     */

    public static String getFileModificationTime(Path filePath) throws IOException {
        return getFileModificationTime(filePath, Clock.system(ZoneOffset.UTC));
    }

    /**
     * Returns a string containing a formatted number of appropriate time units that have elapsed
     * since the file's last modification date. The string is formatted in the ways of
     * "6 seconds ago", "12 minutes ago" or "5 hours ago".
     * @param filePath The file path which modification date is to be parsed.
     * @param currentTimeClock A custom clock to use for retrieving the current system time.
     */
    public static String getFileModificationTime(Path filePath, Clock currentTimeClock) throws IOException {
        FileTime fileTime = Files.getLastModifiedTime(filePath);
        ZonedDateTime zonedFileTime =
                ZonedDateTime.ofInstant(fileTime.toInstant(), currentTimeClock.getZone());

        return formatWithLargestTimeUnit(zonedFileTime);
    }

    private FileDateFormatter() {

    }
}
