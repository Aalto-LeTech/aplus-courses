package fi.aalto.cs.apluscourses.utils

import org.jetbrains.annotations.NonNls
import java.util.Locale

/**
 * A byte count with a fixed two decimal places.
 */
@NonNls
fun formatFileSize(bytes: Long): String {
    var value = bytes.toDouble()
    var unit = 0
    while (value >= STEP && unit < UNITS.lastIndex) {
        value /= STEP
        unit++
    }
    return if (unit == 0) "$bytes ${UNITS[0]}"
    else String.format(Locale.ROOT, "%.2f %s", value, UNITS[unit])
}

@NonNls
private val UNITS = listOf("B", "kB", "MB", "GB", "TB")

private const val STEP = 1000
