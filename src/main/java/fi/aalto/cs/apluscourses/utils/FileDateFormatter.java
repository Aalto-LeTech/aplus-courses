package fi.aalto.cs.apluscourses.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class FileDateFormatter {

  /**
   * Returns a string containing a formatted number of appropriate time units that have elapsed
   * since the file's last modification date. The string is formatted in the ways of
   * "6 seconds ago", "12 minutes ago" or "5 hours ago".
   *
   * @param filePath The file path which modification date is to be parsed.
   */
  public static String getFileModificationTime(Path filePath) throws IOException {
    FileTime fileTime = Files.getLastModifiedTime(filePath);
    ZonedDateTime zonedFileTime = ZonedDateTime.ofInstant(fileTime.toInstant(), ZoneOffset.UTC);
    ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);

    return DateDifferenceFormatter.formatWithLargestTimeUnit(zonedFileTime, currentTime);
  }

  private FileDateFormatter() {

  }
}
