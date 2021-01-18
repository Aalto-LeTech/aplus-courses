package fi.aalto.cs.apluscourses.utils;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DateDifferenceFormatter {
  // the order of time units to check, ordered from most to least precise
  private static final List<ChronoUnit> TimeUnits = Arrays.asList(
          ChronoUnit.SECONDS,
          ChronoUnit.MINUTES,
          ChronoUnit.HOURS,
          ChronoUnit.DAYS,
          ChronoUnit.MONTHS,
          ChronoUnit.YEARS
  );

  private static String formatWithTimeUnit(ZonedDateTime earlierTimePoint,
                                           ZonedDateTime laterTimePoint,
                                           ChronoUnit timeUnit) {
    long timeUnitCount = timeUnit.between(earlierTimePoint, laterTimePoint);
    String timeUnitText = timeUnit.toString().toLowerCase(Locale.ROOT);
    if (timeUnitCount == 1) {
      // remove the final pluralizing "s" from the time unit name
      timeUnitText = timeUnitText.substring(0, timeUnitText.length() - 1);
    }

    return timeUnitCount + " " + timeUnitText + " ago";
  }

  /**
   * Returns a string containing a formatted time difference between two time points. The formatted
   * time is formatted in the ways of "3 seconds ago", "7 months ago" etc. Only the largest possible
   * time unit is used.
   *
   * @param earlierTimePoint The earlier time point.
   * @param laterTimePoint The later time point.
   */
  public static String formatWithLargestTimeUnit(ZonedDateTime earlierTimePoint,
                                                  ZonedDateTime laterTimePoint) {
    if (ChronoUnit.SECONDS.between(earlierTimePoint, laterTimePoint) <= 0) {
      return "just now";
    }

    for (int i = 1; i < TimeUnits.size(); i++) {
      if (TimeUnits.get(i).between(earlierTimePoint, laterTimePoint) == 0) {
        return formatWithTimeUnit(earlierTimePoint, laterTimePoint, TimeUnits.get(i - 1));
      }
    }

    return formatWithTimeUnit(earlierTimePoint, laterTimePoint,
            TimeUnits.get(TimeUnits.size() - 1));
  }

  private DateDifferenceFormatter() {

  }
}
