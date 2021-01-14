package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;

import com.intellij.openapi.util.io.FileUtilRt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.Test;

public class FileDateFormatterTest {

  @Test
  public void testGetFileModificationTime() throws IOException {
    Instant fileTime = Instant.parse("2018-01-03T12:22:20Z");
    Path filePath = FileUtilRt.createTempFile("tempFile", null).toPath();

    Files.setLastModifiedTime(filePath, FileTime.from(fileTime));

    Instant currentClock1 = Instant.parse("2018-01-03T12:23:11Z");
    Instant currentClock2 = Instant.parse("2019-01-03T12:22:20Z");
    Instant currentClock3 = Instant.parse("2019-01-03T12:22:19Z");

    assertEquals(FileDateFormatter.getFileModificationTime(
            filePath, Clock.fixed(currentClock1, ZoneOffset.UTC)), "51 seconds ago");
    assertEquals(FileDateFormatter.getFileModificationTime(
            filePath, Clock.fixed(currentClock2, ZoneOffset.UTC)), "1 year ago");
    assertEquals(FileDateFormatter.getFileModificationTime(
            filePath, Clock.fixed(currentClock3, ZoneOffset.UTC)), "11 months ago");
    assertEquals(FileDateFormatter.getFileModificationTime(
            filePath, Clock.fixed(fileTime, ZoneOffset.UTC)), "just now");
  }
}
