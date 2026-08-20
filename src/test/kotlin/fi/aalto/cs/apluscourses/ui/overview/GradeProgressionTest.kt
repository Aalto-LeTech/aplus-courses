package fi.aalto.cs.apluscourses.ui.overview

import fi.aalto.cs.apluscourses.api.CourseConfig.Grading
import fi.aalto.cs.apluscourses.model.grading.GradeProgress
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class GradeProgressionTest {

    @Test
    fun `the total style folds every category into a single labeled row`() {
        val gradeData = GradeProgress(
            grade = "1",
            pointsUntilNext = mapOf(TOTAL to 3000),
            maxOfNext = mapOf(TOTAL to 15000)
        )

        val progression = gradeProgression(
            style = Grading.STYLE_TOTAL,
            gradeData = gradeData,
            categories = listOf(A, B),
            totalLabel = TOTAL_LABEL
        )

        assertEquals(listOf(TOTAL_LABEL), progression.categories)
        assertEquals(mapOf(TOTAL_LABEL to 3000), progression.pointsUntilNext)
        assertEquals(mapOf(TOTAL_LABEL to 15000), progression.maxOfNext)
    }

    @Test
    fun `the o1 style keeps one row per category`() {
        val gradeData = GradeProgress(
            grade = "1",
            pointsUntilNext = mapOf(A to 100, B to 50),
            maxOfNext = mapOf(A to 200, B to 100)
        )

        val progression = gradeProgression(
            style = Grading.STYLE_O1,
            gradeData = gradeData,
            categories = listOf(A, B),
            totalLabel = TOTAL_LABEL
        )

        assertEquals(listOf(A, B), progression.categories)
        assertEquals(mapOf(A to 100, B to 50), progression.pointsUntilNext)
        assertEquals(mapOf(A to 200, B to 100), progression.maxOfNext)
    }

    @Test
    fun `without grade data the total style still shows the single labeled row`() {
        val progression = gradeProgression(
            style = Grading.STYLE_TOTAL,
            gradeData = null,
            categories = listOf(A, B),
            totalLabel = TOTAL_LABEL
        )

        assertEquals(listOf(TOTAL_LABEL), progression.categories)
        assertNull(progression.pointsUntilNext)
        assertNull(progression.maxOfNext)
    }

    @Test
    fun `without grade data other styles keep one row per category`() {
        val progression = gradeProgression(
            style = Grading.STYLE_O1,
            gradeData = null,
            categories = listOf(A, B),
            totalLabel = TOTAL_LABEL
        )

        assertEquals(listOf(A, B), progression.categories)
        assertNull(progression.pointsUntilNext)
        assertNull(progression.maxOfNext)
    }

    private companion object {
        @NonNls
        const val A = "A"

        @NonNls
        const val B = "B"

        @NonNls
        const val TOTAL = "total"

        @NonNls
        const val TOTAL_LABEL = "Total"
    }
}
