package fi.aalto.cs.apluscourses.utils;

import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

public class DateDifferenceFormatterTest {

  @Test
  public void testFormatWithLargestTimeUnit() {
    ZonedDateTime sampleTime = ZonedDateTime.parse("2018-01-03T12:22:20Z");

    ZonedDateTime currentClock1 = ZonedDateTime.parse("2018-01-03T12:23:11Z");
    ZonedDateTime currentClock2 = ZonedDateTime.parse("2019-01-03T12:22:20Z");
    ZonedDateTime currentClock3 = ZonedDateTime.parse("2019-01-03T12:22:19Z");

    assertEquals(DateDifferenceFormatter.formatWithLargestTimeUnit(
            sampleTime, currentClock1), "51 seconds ago");
    assertEquals(DateDifferenceFormatter.formatWithLargestTimeUnit(
            sampleTime, currentClock2), "1 year ago");
    assertEquals(DateDifferenceFormatter.formatWithLargestTimeUnit(
            sampleTime, currentClock3), "11 months ago");
    assertEquals(DateDifferenceFormatter.formatWithLargestTimeUnit(
            sampleTime, sampleTime), "just now");
  }
}
