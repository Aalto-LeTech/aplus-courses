package fi.aalto.cs.apluscourses.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

class FileSizeFormatterTest {

    @Test
    fun `every size of a kilobyte or more carries exactly two decimals`() {
        val twoDecimals = Regex(TWO_DECIMALS_PATTERN)
        listOf(1_000L, 1_200L, 999_999L, 1_000_000L, 12_345_678L, 5_000_000_000L).forEach {
            val formatted = formatFileSize(it)
            assertTrue(twoDecimals.matches(formatted), "expected two decimals, got: $formatted")
        }
    }

    @Test
    fun `it steps up a unit at a time`() {
        assertEquals("1.00 kB", formatFileSize(1_000))
        assertEquals("1.20 kB", formatFileSize(1_200))
        assertEquals("12.35 MB", formatFileSize(12_345_678))
        assertEquals("5.00 GB", formatFileSize(5_000_000_000))
    }

    @Test
    fun `whole bytes are left undecorated`() {
        assertEquals("0 B", formatFileSize(0))
        assertEquals("512 B", formatFileSize(512))
        assertEquals("999 B", formatFileSize(999))
    }

    private companion object {
        @NonNls
        const val TWO_DECIMALS_PATTERN = """\d+\.\d{2} \w+"""
    }
}
