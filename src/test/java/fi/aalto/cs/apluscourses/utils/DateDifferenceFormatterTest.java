package fi.aalto.cs.apluscourses.utils;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DateDifferenceFormatterTest {

  @Test
  void testFormatWithLargestTimeUnit() {
    ZonedDateTime sampleTime = ZonedDateTime.parse("2018-01-03T12:22:20Z");

    ZonedDateTime currentClock1 = ZonedDateTime.parse("2018-01-03T12:23:11Z");
    ZonedDateTime currentClock2 = ZonedDateTime.parse("2019-01-03T12:22:20Z");
    ZonedDateTime currentClock3 = ZonedDateTime.parse("2019-01-03T12:22:19Z");

    Assertions.assertEquals("51 seconds ago",
        DateDifferenceFormatter.formatWithLargestTimeUnit(sampleTime, currentClock1),
        "Formatter returns the correct value for the '51 seconds' case.");
    Assertions.assertEquals("1 year ago", DateDifferenceFormatter.formatWithLargestTimeUnit(sampleTime, currentClock2),
        "Formatter returns the correct value for the '1 year ago' case.");
    Assertions.assertEquals("11 months ago",
        DateDifferenceFormatter.formatWithLargestTimeUnit(sampleTime, currentClock3),
        "Formatter returns the correct value for the '11 months ago' case.");
    Assertions.assertEquals("just now", DateDifferenceFormatter.formatWithLargestTimeUnit(sampleTime, sampleTime),
        "Formatter returns the correct value for the 'just now' second case.");
  }
}
