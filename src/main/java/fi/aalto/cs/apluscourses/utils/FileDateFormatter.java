package fi.aalto.cs.apluscourses.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.ZonedDateTime;

public class FileDateFormatter {
  /**
   * Same as {@link FileDateFormatter#getFileModificationTime(Path, Clock)} with the clock
   * being always set to the system's current time.
   *
   * @see FileDateFormatter#getFileModificationTime(Path, Clock)
   */

  public static String getFileModificationTime(Path filePath) throws IOException {
    return getFileModificationTime(filePath, Clock.systemUTC()); // any time zone will work here
  }

  /**
   * Returns a string containing a formatted number of appropriate time units that have elapsed
   * since the file's last modification date. The string is formatted in the ways of
   * "6 seconds ago", "12 minutes ago" or "5 hours ago".
   *
   * @param filePath The file path which modification date is to be parsed.
   * @param currentTimeClock A custom clock to use for retrieving the current system time.
   */
  public static String getFileModificationTime(Path filePath, Clock currentTimeClock)
          throws IOException {
    FileTime fileTime = Files.getLastModifiedTime(filePath);
    ZonedDateTime zonedFileTime =
            ZonedDateTime.ofInstant(fileTime.toInstant(), currentTimeClock.getZone());
    ZonedDateTime currentTime = ZonedDateTime.now(currentTimeClock);

    return DateDifferenceFormatter.formatWithLargestTimeUnit(zonedFileTime, currentTime);
  }

  private FileDateFormatter() {

  }
}
