package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.testFramework.junit5.TestApplication
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesTreeRenderer.Companion.submissionCountText
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * A+ uses `max_submissions = 0` for unlimited submissions.
 * The plugin also uses 0 for unknown limits. Neither value is shown as a denominator.
 */
@TestApplication
class SubmissionCountTextTest {
    @Test
    fun `an exercise with a limit shows the count over it`() {
        val exercise = exercise(maxSubmissions = 5, submissions = 3)

        assertTrue(exercise.hasSubmissionLimit)
        assertEquals("3 of 5 ", submissionCountText(exercise))
    }

    @Test
    fun `an unlimited exercise shows the plain count`() {
        val exercise = exercise(maxSubmissions = 0, submissions = 3)

        assertFalse(exercise.hasSubmissionLimit)
        assertEquals("3 ", submissionCountText(exercise))
    }

    @Test
    fun `an unlimited exercise keeps its difficulty and late marker`() {
        val exercise = exercise(maxSubmissions = 0, submissions = 3, difficulty = "A", latePenalty = 0.5)

        assertEquals("late, 3 A", submissionCountText(exercise))
    }

    private fun exercise(
        maxSubmissions: Int,
        submissions: Int,
        @NonNls difficulty: String = "",
        latePenalty: Double? = null
    ): Exercise {
        val results = (1..submissions).map { index ->
            SubmissionResult(
                id = index.toLong(),
                maxPoints = 10,
                userPoints = 0,
                latePenalty = latePenalty,
                status = SubmissionResult.Status.GRADED,
                isSubmittable = true,
                submitters = null
            )
        }
        return Exercise(
            id = 10,
            name = "1.1 Exercise",
            module = null,
            htmlUrl = "",
            submissionResults = results.toMutableList(),
            maxPoints = 10,
            userPoints = 0,
            maxSubmissions = maxSubmissions,
            bestSubmissionId = results.firstOrNull()?.id,
            difficulty = difficulty,
            isOptional = false,
            isSubmittable = true,
            isFeedback = false
        )
    }
}
