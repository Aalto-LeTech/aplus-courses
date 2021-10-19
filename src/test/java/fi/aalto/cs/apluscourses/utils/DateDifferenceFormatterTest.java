package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

public class DateDifferenceFormatterTest {

  @Test
  public void testFormatWithLargestTimeUnit() {
    ZonedDateTime sampleTime = ZonedDateTime.parse("2018-01-03T12:22:20Z");

    ZonedDateTime currentClock1 = ZonedDateTime.parse("2018-01-03T12:23:11Z");
    ZonedDateTime currentClock2 = ZonedDateTime.parse("2019-01-03T12:22:20Z");
    ZonedDateTime currentClock3 = ZonedDateTime.parse("2019-01-03T12:22:19Z");

    assertEquals("Formatter returns the correct value for the '51 seconds' case.",
        "51 seconds ago",
        DateDifferenceFormatter.formatWithLargestTimeUnit(sampleTime, currentClock1));
    assertEquals("Formatter returns the correct value for the '1 year ago' case.",
        "1 year ago",
        DateDifferenceFormatter.formatWithLargestTimeUnit(sampleTime, currentClock2));
    assertEquals("Formatter returns the correct value for the '11 months ago' case.",
        "11 months ago",
        DateDifferenceFormatter.formatWithLargestTimeUnit(sampleTime, currentClock3));
    assertEquals("Formatter returns the correct value for the 'just now' second case.",
        "just now",
        DateDifferenceFormatter.formatWithLargestTimeUnit(sampleTime, sampleTime));
  }
}
