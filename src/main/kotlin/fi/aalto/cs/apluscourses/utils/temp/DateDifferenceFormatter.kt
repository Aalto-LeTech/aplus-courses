package fi.aalto.cs.apluscourses.utils.temp

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import java.time.temporal.ChronoUnit

object DateDifferenceFormatter {
    fun formatTimeUntilNow(time: Instant): String {
        val currentTime = Clock.System.now()
        return formatWithLargestTimeUnit(time, currentTime, true)
    }

    fun formatTimeSinceNow(time: Instant): String {
        val currentTime = Clock.System.now()
        return formatWithLargestTimeUnit(currentTime, time, false)
    }

    /**
     * Returns a string containing a formatted time difference between two time points. The formatted
     * time is formatted in the ways of "3 seconds ago", "7 months ago" etc. Only the largest possible
     * time unit is used.
     *
     * @param earlierTimePoint The earlier time point.
     * @param laterTimePoint   The later time point.
     */
    fun formatWithLargestTimeUnit(
        earlierTimePoint: Instant,
        laterTimePoint: Instant,
        before: Boolean
    ): String {
        val period = earlierTimePoint.periodUntil(laterTimePoint, TimeZone.UTC)

        if (period.seconds <= 0) {
            return "just now"
        }

        val (unit, time) = when {
            period.years > 0 -> ChronoUnit.YEARS to period.years
            period.months > 0 -> ChronoUnit.MONTHS to period.months
            period.days > 0 -> ChronoUnit.DAYS to period.days
            period.hours > 0 -> ChronoUnit.HOURS to period.hours
            period.minutes > 0 -> ChronoUnit.MINUTES to period.minutes
            else -> ChronoUnit.SECONDS to period.seconds
        }

        var timeUnitText = unit.toString().lowercase()
        if (time == 1) {
            // remove the final pluralizing "s" from the time unit name
            timeUnitText = timeUnitText.substringBeforeLast("s")
        }

        return if (before) {
            "$time $timeUnitText ago"
        } else {
            "in $time $timeUnitText"
        }
    }
}
