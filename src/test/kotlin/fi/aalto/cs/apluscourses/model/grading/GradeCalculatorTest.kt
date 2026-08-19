package fi.aalto.cs.apluscourses.model.grading

import fi.aalto.cs.apluscourses.api.CourseConfig.Grading
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

class GradeCalculatorTest {

    private val o1 = Grading(
        style = Grading.STYLE_O1,
        points = linkedMapOf(
            "1" to mapOf(A to 100, B to 0, C to 0),
            "2" to mapOf(A to 200, B to 100, C to 0),
            "3" to mapOf(A to 300, B to 200, C to 100)
        )
    )

    private fun o1Points(a: Int, b: Int, c: Int) = mapOf(A to a, B to b, C to c)

    @Test
    fun `an unsupported style has no grade progression`() {
        val custom = Grading(style = UNSUPPORTED_STYLE, points = mapOf("1" to mapOf(TOTAL to 10)))

        assertNull(GradeCalculator.calculate(custom, mapOf(TOTAL to 5)))
    }

    @Test
    fun `the grade is the highest one whose requirements are all met`() {
        val progress = GradeCalculator.calculate(o1, o1Points(a = 300, b = 200, c = 100))

        assertEquals("3", progress?.grade)
    }

    @Test
    fun `surplus points in higher categories count towards A`() {
        val progress = GradeCalculator.calculate(o1, o1Points(a = 40, b = 40, c = 40))

        assertEquals("1", progress?.grade, "B and C should have been carried over into A")
    }

    @Test
    fun `points carried into A are no longer available for B`() {
        val progress = GradeCalculator.calculate(o1, o1Points(a = 100, b = 100, c = 0))

        assertEquals("1", progress?.grade)
        assertEquals(0, progress?.pointsUntilNext?.get(A), "A was lifted to 200 by the carry-over")
        assertEquals(100, progress?.pointsUntilNext?.get(B), "B gave its points away to A")
    }

    @Test
    fun `points until the next grade are reported per category`() {
        val progress = GradeCalculator.calculate(o1, o1Points(a = 100, b = 0, c = 0))

        assertEquals("1", progress?.grade)
        assertEquals(mapOf(A to 100, B to 100, C to 0), progress?.pointsUntilNext)
        assertEquals(mapOf(A to 200, B to 100, C to 0), progress?.maxOfNext)
    }

    @Test
    fun `the highest grade reports zero points remaining rather than nothing`() {
        val progress = GradeCalculator.calculate(o1, o1Points(a = 500, b = 300, c = 200))

        assertEquals("3", progress?.grade)
        assertEquals(mapOf(A to 0, B to 0, C to 0), progress?.pointsUntilNext)
    }

    @Test
    fun `a missing category means no grade can be calculated`() {
        assertNull(GradeCalculator.calculate(o1, mapOf(A to 100, B to 100)))
    }

    @Test
    fun `the total style sums every category`() {
        val total = Grading(
            style = Grading.STYLE_TOTAL,
            points = linkedMapOf("1" to mapOf(TOTAL to 100), "2" to mapOf(TOTAL to 200))
        )

        val progress = GradeCalculator.calculate(total, mapOf(A to 60, B to 60))

        assertEquals("1", progress?.grade, "120 points should reach grade 1 but not grade 2")
        assertEquals(80, progress?.pointsUntilNext?.get(TOTAL))
        assertEquals(200, progress?.maxOfNext?.get(TOTAL))
    }

    private companion object {
        @NonNls
        const val A = "A"

        @NonNls
        const val B = "B"

        @NonNls
        const val C = "C"

        @NonNls
        const val TOTAL = "total"

        @NonNls
        const val UNSUPPORTED_STYLE = "custom"
    }
}
