package fi.aalto.cs.apluscourses.services.exercise

import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater.Companion.assembleExerciseGroups
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater.Companion.computeCategoryPoints
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater.Companion.computeChangeSnapshot
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExercisesUpdaterLogicTest {
    @Test
    fun `exercises with an empty difficulty are excluded from category points`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(maxPoints = 10),
                        pointsExercise(id = 2, difficulty = CATEGORY_A, maxPoints = 5)
                    )
                )
            )
        )

        val result = computeCategoryPoints(points)

        assertEquals(setOf(CATEGORY_A), result.maxPointsForCategories.keys, "the empty category should not be tracked")
        assertEquals(5, result.maxPointsForCategories[CATEGORY_A])
    }

    @Test
    fun `a category absent from pointsByDifficulty gets 0 user points`() {
        val points = pointsBody(
            pointsByDifficulty = emptyMap(),
            modules = listOf(pointsModule(exercises = listOf(pointsExercise(difficulty = CATEGORY_A, maxPoints = 20))))
        )

        val result = computeCategoryPoints(points)

        assertEquals(0, result.userPointsForCategories[CATEGORY_A])
    }

    @Test
    fun `course totals come from the API total and the modules' max points`() {
        val points = pointsBody(
            points = 15,
            modules = listOf(
                pointsModule(maxPoints = 100),
                pointsModule(id = 2, maxPoints = 200)
            )
        )

        val result = computeCategoryPoints(points)

        assertEquals(15, result.userPointsTotal)
        assertEquals(300, result.maxPointsTotal)
    }

    @Test
    fun `max points for a category are summed across modules`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(exercises = listOf(pointsExercise(difficulty = CATEGORY_A, maxPoints = 10))),
                pointsModule(
                    id = 2,
                    exercises = listOf(pointsExercise(id = 2, difficulty = CATEGORY_A, maxPoints = 15))
                )
            )
        )

        val result = computeCategoryPoints(points)

        assertEquals(25, result.maxPointsForCategories[CATEGORY_A])
    }

    @Test
    fun `the first run, with no existing exercise groups, is always changed`() {
        val points = pointsBody(modules = listOf(pointsModule(exercises = listOf(pointsExercise(points = 5)))))

        val result = computeChangeSnapshot(
            points,
            previousPoints = 5,
            previousSubmissionCount = 0,
            hasExerciseGroups = false
        )

        assertTrue(result.changed, "there's no existing tree to compare against, so it must be rebuilt")
    }

    @Test
    fun `identical totals with an existing tree are unchanged`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(exercises = listOf(pointsExercise(points = 5, submissions = listOf(SUBMISSION_1))))
            )
        )

        val result = computeChangeSnapshot(
            points,
            previousPoints = 5,
            previousSubmissionCount = 1,
            hasExerciseGroups = true
        )

        assertFalse(result.changed)
        assertEquals(5, result.newPoints)
        assertEquals(1, result.newSubmissionCount)
    }

    @Test
    fun `a change in total points is detected`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(exercises = listOf(pointsExercise(points = 8, submissions = listOf(SUBMISSION_1))))
            )
        )

        val result = computeChangeSnapshot(
            points,
            previousPoints = 5,
            previousSubmissionCount = 1,
            hasExerciseGroups = true
        )

        assertTrue(result.changed)
    }

    @Test
    fun `a change in submission count is detected`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(pointsExercise(points = 5, submissions = listOf(SUBMISSION_1, SUBMISSION_2)))
                )
            )
        )

        val result = computeChangeSnapshot(
            points,
            previousPoints = 5,
            previousSubmissionCount = 1,
            hasExerciseGroups = true
        )

        assertTrue(result.changed)
    }

    @Test
    fun `fields are mapped, including localized names`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    name = "|en:Module|fi:Moduuli|",
                    exercises = listOf(
                        pointsExercise(
                            id = 10,
                            name = "|en:Exercise|fi:Tehtava|",
                            url = EXERCISE_URL,
                            maxPoints = 5,
                            points = 3
                        )
                    )
                )
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_FI,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        assertEquals(1, groups.size)
        assertEquals("Moduuli", groups[0].name)
        val exercise = groups[0].exercises[0]
        assertEquals(10L, exercise.id)
        assertEquals("Tehtava", exercise.name)
        assertEquals(EXERCISE_URL, exercise.url)
        assertEquals(5, exercise.maxPoints)
        assertEquals(3, exercise.userPoints)
    }

    @Test
    fun `missing exercise details fall back to an empty url, not submittable, zero max submissions`() {
        val points = pointsBody(modules = listOf(pointsModule(exercises = listOf(pointsExercise(id = 10)))))

        // No matching entry in exerciseDetails for module/exercise id 1/10.
        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        val exercise = groups[0].exercises[0]
        assertEquals("", exercise.htmlUrl)
        assertFalse(exercise.isSubmittable)
        assertEquals(0, exercise.maxSubmissions)
    }

    @Test
    fun `module and exercise details are joined by id, not by list position`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(exercises = listOf(pointsExercise(id = 10), pointsExercise(id = 11))),
                pointsModule(id = 2, exercises = listOf(pointsExercise(id = 20)))
            )
        )

        val exerciseDetails = listOf(
            courseModule(
                id = 2,
                htmlUrl = "https://example.com/modules/2",
                isOpen = false,
                closingTime = "2026-01-01T00:00:00Z",
                exercises = listOf(
                    exerciseDetail(
                        id = 20,
                        htmlUrl = "https://example.com/exercises/20",
                        maxSubmissions = 7,
                        hasSubmittableFiles = true
                    )
                )
            ),
            courseModule(
                id = 1,
                htmlUrl = "https://example.com/modules/1",
                isOpen = true,
                closingTime = "2026-02-02T00:00:00Z",
                exercises = listOf(
                    exerciseDetail(
                        id = 11,
                        htmlUrl = "https://example.com/exercises/11",
                        maxSubmissions = 3,
                        hasSubmittableFiles = true
                    ),
                    exerciseDetail(
                        id = 10,
                        htmlUrl = "https://example.com/exercises/10",
                        maxSubmissions = 5,
                        hasSubmittableFiles = false
                    )
                )
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = exerciseDetails,
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        val group1 = groups.single { it.id == 1L }
        val group2 = groups.single { it.id == 2L }
        assertEquals("https://example.com/modules/1", group1.htmlUrl)
        assertTrue(group1.isOpen)
        assertEquals("2026-02-02T00:00:00Z", group1.closingTime)
        assertEquals("https://example.com/modules/2", group2.htmlUrl)
        assertFalse(group2.isOpen)
        assertEquals("2026-01-01T00:00:00Z", group2.closingTime)

        val exercise10 = group1.exercises.single { it.id == 10L }
        val exercise11 = group1.exercises.single { it.id == 11L }
        val exercise20 = group2.exercises.single { it.id == 20L }
        assertEquals("https://example.com/exercises/10", exercise10.htmlUrl)
        assertEquals(5, exercise10.maxSubmissions)
        assertFalse(exercise10.isSubmittable, "exercise 10's own details say hasSubmittableFiles = false")
        assertEquals("https://example.com/exercises/11", exercise11.htmlUrl)
        assertEquals(3, exercise11.maxSubmissions)
        assertTrue(exercise11.isSubmittable, "exercise 11's own details say hasSubmittableFiles = true")
        assertEquals("https://example.com/exercises/20", exercise20.htmlUrl)
        assertEquals(7, exercise20.maxSubmissions)
        assertTrue(exercise20.isSubmittable)
    }

    @Test
    fun `submission data overrides the points API grade and penalty`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(
                            id = 10,
                            submissionsWithPoints = listOf(submissionWithPoints(id = 100, grade = 3))
                        )
                    )
                )
            )
        )
        val submissionData = listOf(submissionDataBody(submissionId = 100, userId = 1, grade = 9, penalty = 0.5))

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = submissionData,
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        val submission = groups[0].exercises[0].submissionResults[0]
        assertEquals(9, submission.userPoints, "submission data's grade should win over the points API's")
        assertEquals(0.5, submission.latePenalty)
    }

    @Test
    fun `the points API grade is kept when there is no matching submission data`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(
                            id = 10,
                            submissionsWithPoints = listOf(submissionWithPoints(id = 100, grade = 3))
                        )
                    )
                )
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        assertEquals(3, groups[0].exercises[0].submissionResults[0].userPoints)
    }

    @Test
    fun `submission status is mapped through statusFromString, falling back to UNKNOWN without submission data`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(
                            id = 10,
                            submissionsWithPoints = listOf(
                                submissionWithPoints(id = 100),
                                submissionWithPoints(id = 200)
                            )
                        )
                    )
                )
            )
        )
        val submissionData = listOf(submissionDataBody(submissionId = 100, userId = 1, status = "ready"))

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = submissionData,
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        val submissions = groups[0].exercises[0].submissionResults.associateBy { it.id }
        assertEquals(
            SubmissionResult.Status.GRADED,
            submissions.getValue(100L).status,
            "submission data status \"ready\" maps to GRADED"
        )
        assertEquals(
            SubmissionResult.Status.UNKNOWN,
            submissions.getValue(200L).status,
            "no matching submission data falls back to UNKNOWN"
        )
    }

    @Test
    fun `submitters are attached only when a submission has more than one submitter row`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(
                            id = 10,
                            submissionsWithPoints = listOf(
                                submissionWithPoints(id = 100),
                                submissionWithPoints(id = 200)
                            )
                        )
                    )
                )
            )
        )
        val submissionData = listOf(
            submissionDataBody(submissionId = 100, userId = 1),
            submissionDataBody(submissionId = 100, userId = 2),
            submissionDataBody(submissionId = 200, userId = 3)
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = submissionData,
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        val exercise = groups[0].exercises[0]
        val groupSubmission = exercise.submissionResults.single { it.id == 100L }
        val soloSubmission = exercise.submissionResults.single { it.id == 200L }
        assertEquals(listOf(1L, 2L), groupSubmission.submitters)
        assertNull(soloSubmission.submitters, "a submission with a single submitter row isn't a group submission")
    }

    @Test
    fun `in a categorized course an empty difficulty counts as optional regardless of the configured optional categories`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(id = 10),
                        pointsExercise(id = 11, difficulty = CATEGORY_B)
                    )
                )
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = listOf(CATEGORY_A),
            exerciseModules = emptyMap()
        )

        assertTrue(groups[0].exercises.first { it.id == 10L }.isOptional)
    }

    @Test
    fun `a course with no difficulty categories has no optional exercises`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(id = 10),
                        pointsExercise(id = 11)
                    )
                )
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        assertTrue(groups[0].exercises.none { it.isOptional })
    }

    @Test
    fun `a non-empty difficulty listed in optionalCategories is optional, one that isn't listed is not`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(id = 10, difficulty = CATEGORY_A),
                        pointsExercise(id = 11, difficulty = CATEGORY_B)
                    )
                )
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = listOf(CATEGORY_A),
            exerciseModules = emptyMap()
        )

        val exercises = groups[0].exercises.associateBy { it.id }
        assertTrue(exercises.getValue(10L).isOptional, "difficulty \"A\" is listed in optionalCategories")
        assertFalse(exercises.getValue(11L).isOptional, "difficulty \"B\" isn't listed in optionalCategories")
    }

    @Test
    fun `isFeedback requires both the feedback marker and an empty difficulty`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(id = 10, name = "|en:Feedback|fi:Palaute|"),
                        pointsExercise(id = 11, name = "|en:Feedback|fi:Palaute|", difficulty = CATEGORY_A),
                        pointsExercise(id = 12, name = "|en:Other|fi:Muu|")
                    )
                )
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        val exercises = groups[0].exercises.associateBy { it.id }
        assertTrue(exercises.getValue(10L).isFeedback, "has both the marker and an empty difficulty")
        assertFalse(exercises.getValue(11L).isFeedback, "has a non-empty difficulty")
        assertFalse(exercises.getValue(12L).isFeedback, "lacks the feedback marker")
    }

    @Test
    fun `bestSubmissionId is parsed from the tail of the submission URL`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(
                    exercises = listOf(
                        pointsExercise(id = 10, bestSubmission = "https://example.com/api/v2/submissions/42")
                    )
                )
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        assertEquals(42L, groups[0].exercises[0].bestSubmissionId)
    }

    @Test
    fun `a malformed bestSubmission URL yields a null id instead of throwing`() {
        val points = pointsBody(
            modules = listOf(
                pointsModule(exercises = listOf(pointsExercise(id = 10, bestSubmission = "not-a-number")))
            )
        )

        val groups = assembleExerciseGroups(
            points = points,
            exerciseDetails = emptyList(),
            submissionData = emptyList(),
            language = LANGUAGE_EN,
            optionalCategories = emptyList(),
            exerciseModules = emptyMap()
        )

        assertNull(groups[0].exercises[0].bestSubmissionId)
    }

    private fun pointsBody(
        pointsByDifficulty: Map<String, Int> = emptyMap(),
        points: Int = 0,
        modules: List<APlusApi.Course.Points.Module> = emptyList()
    ) = APlusApi.Course.Points.PointsBody(
        id = 1,
        url = "",
        username = USERNAME,
        studentId = null,
        email = "",
        fullName = "",
        isExternal = false,
        tags = emptyList(),
        role = STUDENT_ROLE,
        submissionCount = 0,
        points = points,
        pointsByDifficulty = pointsByDifficulty,
        modules = modules
    )

    private fun courseModule(
        id: Long,
        @NonNls url: String = "",
        @NonNls htmlUrl: String = "",
        @NonNls displayName: String = "",
        isOpen: Boolean,
        @NonNls closingTime: String = "",
        exercises: List<APlusApi.Course.Exercises.Exercise> = emptyList()
    ) = APlusApi.Course.Exercises.CourseModule(
        id = id,
        url = url,
        htmlUrl = htmlUrl,
        displayName = displayName,
        isOpen = isOpen,
        closingTime = closingTime,
        exercises = exercises
    )

    private fun exerciseDetail(
        id: Long,
        @NonNls url: String = "",
        @NonNls htmlUrl: String = "",
        @NonNls displayName: String = "",
        maxPoints: Int = 0,
        maxSubmissions: Int = 0,
        @NonNls hierarchicalName: String = "",
        @NonNls difficulty: String = "",
        hasSubmittableFiles: Boolean
    ) = APlusApi.Course.Exercises.Exercise(
        id = id,
        url = url,
        htmlUrl = htmlUrl,
        displayName = displayName,
        maxPoints = maxPoints,
        maxSubmissions = maxSubmissions,
        hierarchicalName = hierarchicalName,
        difficulty = difficulty,
        hasSubmittableFiles = hasSubmittableFiles
    )

    private fun pointsModule(
        id: Long = 1,
        @NonNls name: String = "|en:Module|",
        maxPoints: Int = 0,
        points: Int = 0,
        exercises: List<APlusApi.Course.Points.Exercise> = emptyList()
    ) = APlusApi.Course.Points.Module(
        id = id,
        name = name,
        maxPoints = maxPoints,
        pointsToPass = 0,
        submissionCount = 0,
        points = points,
        pointsByDifficulty = emptyMap(),
        passed = false,
        exercises = exercises
    )

    private fun pointsExercise(
        id: Long = 1,
        @NonNls name: String = "|en:Exercise|",
        @NonNls url: String = "",
        @NonNls difficulty: String = "",
        maxPoints: Int = 0,
        points: Int = 0,
        @NonNls submissions: List<String> = emptyList(),
        submissionsWithPoints: List<APlusApi.Course.Points.SubmissionWithPoints> = emptyList(),
        @NonNls bestSubmission: String? = null
    ) = APlusApi.Course.Points.Exercise(
        url = url,
        bestSubmission = bestSubmission,
        submissions = submissions,
        submissionsWithPoints = submissionsWithPoints,
        id = id,
        name = name,
        difficulty = difficulty,
        maxPoints = maxPoints,
        pointsToPass = 0,
        submissionCount = submissions.size,
        points = points,
        passed = false,
        official = true
    )

    private fun submissionWithPoints(
        id: Long,
        @NonNls url: String = "",
        grade: Int = 0
    ) = APlusApi.Course.Points.SubmissionWithPoints(
        id = id,
        url = url,
        submissionTime = "",
        grade = grade
    )

    private fun submissionDataBody(
        submissionId: Long,
        userId: Long,
        @NonNls status: String = "waiting",
        grade: Int = 0,
        penalty: Double? = null
    ) = APlusApi.Course.SubmissionData.SubmissionDataBody(
        submissionId = submissionId,
        userId = userId,
        status = status,
        grade = grade,
        penalty = penalty
    )

    private companion object {
        @NonNls
        const val LANGUAGE_EN = "en"

        @NonNls
        const val LANGUAGE_FI = "fi"

        @NonNls
        const val CATEGORY_A = "A"

        @NonNls
        const val CATEGORY_B = "B"

        @NonNls
        const val EXERCISE_URL = "https://example.com/exercises/10"

        @NonNls
        const val SUBMISSION_1 = "s1"

        @NonNls
        const val SUBMISSION_2 = "s2"

        @NonNls
        const val USERNAME = "user"

        @NonNls
        const val STUDENT_ROLE = "student"
    }
}
