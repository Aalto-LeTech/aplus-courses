package fi.aalto.cs.apluscourses.model.grading

import fi.aalto.cs.apluscourses.api.CourseConfig.Grading
import org.jetbrains.annotations.NonNls

/**
 * The student's current grade and how many points are needed until the next grade.
 *
 * [pointsUntilNext] and [maxOfNext] are per category.
 */
data class GradeProgress(
    val grade: String?,
    val pointsUntilNext: Map<String, Int>,
    val maxOfNext: Map<String, Int>
)

/**
 * Calculates a grade from the course's grading rules and the student's points.
 */
object GradeCalculator {
    @NonNls
    private const val A = "A"

    @NonNls
    private const val B = "B"

    @NonNls
    private const val C = "C"

    fun calculate(grading: Grading, points: Map<String, Int>): GradeProgress? =
        when (grading.style) {
            Grading.STYLE_O1 -> o1(grading, points)
            Grading.STYLE_TOTAL -> total(grading, points)
            else -> null
        }

    /**
     * Points earned in a higher category count towards a lower one: surplus B and C fill A, then
     * any C left over fills B. The grade is then the last one whose requirements all hold.
     */
    private fun o1(grading: Grading, points: Map<String, Int>): GradeProgress? {
        val gradePoints = grading.points
        val aPoints = points[A] ?: return null
        var bPoints = points[B] ?: return null
        var cPoints = points[C] ?: return null

        val highest = gradePoints.entries.lastOrNull()?.value ?: return null

        // Calculate effective points for A and adjust B and C accordingly
        val requiredA = highest[A] ?: return null
        val neededA = maxOf(0, requiredA - aPoints)
        val usedBForA = minOf(bPoints, neededA)
        val usedCForA = minOf(cPoints, neededA - usedBForA)
        val finalA = aPoints + usedBForA + usedCForA

        bPoints -= usedBForA
        cPoints -= usedCForA

        // Calculate effective points for B
        val requiredB = highest[B] ?: return null
        val neededB = maxOf(0, requiredB - bPoints)
        val usedCForB = minOf(cPoints, neededB)
        val effectiveB = bPoints + usedCForB

        cPoints -= usedCForB

        val effective = mapOf(A to finalA, B to effectiveB, C to cPoints)

        val currentGrade = gradePoints.entries.findLast { (_, required) ->
            effective.all { (category, earned) -> earned >= (required[category] ?: 0) }
        }
        val nextGrade = gradePoints.entries.firstOrNull { (_, required) ->
            effective.any { (category, earned) -> earned < (required[category] ?: 0) }
        }

        val pointsUntilNext = nextGrade?.value
            ?.mapValues { (category, required) -> maxOf(0, required - (effective[category] ?: 0)) }
            ?: effective.keys.associateWith { 0 }

        return GradeProgress(
            grade = currentGrade?.key,
            pointsUntilNext = pointsUntilNext,
            maxOfNext = effective.keys.associateWith { nextGrade?.value?.get(it) ?: 0 }
        )
    }

    /** Every category counts towards a single total. */
    private fun total(grading: Grading, points: Map<String, Int>): GradeProgress {
        val key = Grading.STYLE_TOTAL
        val gradePoints = grading.points
        val totalPoints = points.values.sum()

        val currentGrade = gradePoints.entries.findLast { (_, required) ->
            totalPoints >= (required[key] ?: 0)
        }
        val nextGrade = gradePoints.entries.firstOrNull { (_, required) ->
            totalPoints < (required[key] ?: 0)
        }

        return GradeProgress(
            grade = currentGrade?.key,
            pointsUntilNext = mapOf(key to maxOf(0, (nextGrade?.value?.get(key) ?: totalPoints) - totalPoints)),
            maxOfNext = mapOf(key to (nextGrade?.value?.get(key) ?: 0))
        )
    }
}
