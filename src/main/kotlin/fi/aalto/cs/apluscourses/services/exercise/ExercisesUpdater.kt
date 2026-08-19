package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.notifications.FeedbackAvailableNotification
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.TokenStorage
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import org.jetbrains.annotations.NonNls
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds


@Service(Service.Level.PROJECT)
class ExercisesUpdater(
    val project: Project,
    val cs: CoroutineScope
) {
    class State {
        var exerciseGroups: MutableList<ExerciseGroup> = mutableListOf()
        var userPointsForCategories: Map<String, Int>? = null
        var maxPointsForCategories: Map<String, Int>? = null

        var userPointsTotal: Int? = null
        var maxPointsTotal: Int? = null

        fun clearAll() {
            exerciseGroups.clear()
            userPointsForCategories = null
            maxPointsForCategories = null
            userPointsTotal = null
            maxPointsTotal = null
        }
    }

    val state: State = State()

    @Volatile
    private var exerciseJob: Job? = null

    @Volatile
    private var gradingJob: Job? = null

    val isRunning: Boolean
        get() = exerciseJob?.isActive == true

    private val submissionsInGrading: MutableSet<Long> = ConcurrentHashMap.newKeySet()
    private var lastSubmissionCount = -1
    private var lastPoints = -1

    fun restart(force: Boolean = false) {
        val previousExerciseJob = exerciseJob
        previousExerciseJob?.cancel()
        exerciseJob = runExerciseUpdater(previousExerciseJob, force)
    }

    private fun runExerciseUpdater(previous: Job?, force: Boolean): Job =
        cs.launch {
            previous?.join()
            try {
                withBackgroundProgress(
                    project,
                    message("services.progress.refreshingAssignments"),
                    cancellable = true
                ) {
                    updateExercises(force)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                handleNetworkError(e)
            } catch (e: UnresolvedAddressException) {
                handleNetworkError(e)
            }
        }

    private fun resetChangeSnapshot() {
        lastPoints = -1
        lastSubmissionCount = -1
    }

    private fun handleNetworkError(e: Exception) {
        CoursesLogger.warn("Network error in ExercisesUpdater", e)
        resetChangeSnapshot()
        CourseManager.getInstance(project).fireNetworkError(e)
    }

    private fun handleGradingNetworkError(e: Exception) {
        CoursesLogger.warn("Network error in grading updater", e)
        resetChangeSnapshot()
    }

    private fun runGradingUpdater(previous: Job?): Job =
        cs.launch {
            previous?.join()
            try {
                withBackgroundProgress(
                    project,
                    message("services.progress.gradingAssignments"),
                    cancellable = true
                ) {
                    while (submissionsInGrading.isNotEmpty()) {
                        pollGradedSubmissions()
                        if (submissionsInGrading.isEmpty()) break
                        delay(GRADING_POLL_INTERVAL)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                handleGradingNetworkError(e)
            } catch (e: UnresolvedAddressException) {
                handleGradingNetworkError(e)
            }
        }

    private suspend fun updateExercises(force: Boolean) {
        if (!TokenStorage.getInstance().isTokenSet()) {
            CoursesLogger.info("Not authenticated, clearing exercises")
            state.clearAll()
            fireExercisesUpdated()
            return
        }
        val course = CourseManager.course(project) ?: return
        CoursesLogger.info("Updating exercises for course ${course.id}")
        val timeStart = System.currentTimeMillis()
        val language = CourseFileManager.getInstance(project).state.language
        if (language == null) {
            CoursesLogger.warn("Course language not set, skipping exercise update")
            return
        }

        val fetched = fetchIfChanged(course, force)
        if (fetched == null) {
            CoursesLogger.info("No changes in exercises")
            return
        }

        val newExerciseGroups = buildExerciseGroups(course, language, fetched)
        state.exerciseGroups.clear()
        state.exerciseGroups.addAll(newExerciseGroups)
        fireExercisesUpdated()
        restartGradingForWaitingSubmissions(newExerciseGroups)
        CoursesLogger.info("Done updating exercises. Time taken: ${System.currentTimeMillis() - timeStart} ms")
    }

    private class FetchedExercises(
        val points: APlusApi.Course.Points.PointsBody,
        val exercises: List<APlusApi.Course.Exercises.CourseModule>,
        val submissionData: List<APlusApi.Course.SubmissionData.SubmissionDataBody>
    )

    /**
     * Fetches the data needed to rebuild the exercise tree, or returns null if nothing has
     * changed since the last refresh and [force] is false.
     */
    private suspend fun fetchIfChanged(course: Course, force: Boolean): FetchedExercises? {
        val courseApi = APlusApi.course(course)
        val points = withContext(Dispatchers.IO) { courseApi.points(project) }
        updateCategoryPoints(points)
        val changed = updateChangeSnapshot(points)
        if (!changed && !force) return null
        return coroutineScope {
            val exercisesDeferred = async(Dispatchers.IO) { courseApi.exercises(project) }
            val submissionDataDeferred = async(Dispatchers.IO) { courseApi.submissionData(project) }
            FetchedExercises(points, exercisesDeferred.await(), submissionDataDeferred.await())
        }
    }

    private fun updateCategoryPoints(points: APlusApi.Course.Points.PointsBody) {
        val categoryPoints = computeCategoryPoints(points)
        state.maxPointsForCategories = categoryPoints.maxPointsForCategories
        state.userPointsForCategories = categoryPoints.userPointsForCategories
        state.userPointsTotal = categoryPoints.userPointsTotal
        state.maxPointsTotal = categoryPoints.maxPointsTotal
        firePointsByDifficultyUpdated()
    }

    /**
     * Compares the total points and submission count against the previous refresh and remembers
     * the new values. Returns false when the exercise tree is already up to date.
     */
    private fun updateChangeSnapshot(points: APlusApi.Course.Points.PointsBody): Boolean {
        val snapshot = computeChangeSnapshot(
            points,
            previousPoints = lastPoints,
            previousSubmissionCount = lastSubmissionCount,
            hasExerciseGroups = state.exerciseGroups.isNotEmpty()
        )
        if (snapshot.changed) {
            lastPoints = snapshot.newPoints
            lastSubmissionCount = snapshot.newSubmissionCount
        }
        return snapshot.changed
    }

    private fun buildExerciseGroups(
        course: Course,
        language: String,
        fetched: FetchedExercises
    ): List<ExerciseGroup> = assembleExerciseGroups(
        points = fetched.points,
        exerciseDetails = fetched.exercises,
        submissionData = fetched.submissionData,
        language = language,
        optionalCategories = course.optionalCategories,
        exerciseModules = course.exerciseModules
    )

    private fun restartGradingForWaitingSubmissions(exerciseGroups: List<ExerciseGroup>) {
        val waiting = exerciseGroups
            .flatMap { it.exercises }
            .flatMap { it.submissionResults }
            .filter { it.status == SubmissionResult.Status.WAITING && it.isSubmittable }
            .map { it.id }
        submissionsInGrading.clear()
        submissionsInGrading.addAll(waiting)
        if (submissionsInGrading.isNotEmpty()) {
            val previous = gradingJob
            previous?.cancel()
            gradingJob = runGradingUpdater(previous)
        }
    }

    private suspend fun pollGradedSubmissions() {
        var anyGraded = false
        for (submissionId in submissionsInGrading.toList()) {
            val submission = APlusApi.Submission(submissionId).get(project)
            if (SubmissionResult.statusFromString(submission.status) == SubmissionResult.Status.WAITING) continue
            anyGraded = true
            submissionsInGrading.remove(submissionId)
            applyGradedSubmission(submissionId, submission)
        }
        // A graded submission changes points, so the whole exercise tree needs a refresh
        if (anyGraded) restart()
    }

    private fun applyGradedSubmission(submissionId: Long, submission: APlusApi.Submission.SubmissionBody) {
        val exercise = state.exerciseGroups
            .flatMap { it.exercises }
            .find { it.id == submission.exercise.id } ?: return
        val submissionResult = exercise.submissionResults.find { it.id == submissionId } ?: return
        submissionResult.updateStatus(submission.status)
        submissionResult.userPoints = submission.grade
        submissionResult.latePenalty = submission.latePenaltyApplied
        Notifier.notify(FeedbackAvailableNotification(submissionResult, exercise, project), project)
        fireExerciseUpdated(exercise)
    }

    private fun fireExercisesUpdated() = fire { onExercisesUpdated() }

    private fun fireExerciseUpdated(exercise: Exercise) = fire { onExerciseUpdated(exercise) }

    private fun firePointsByDifficultyUpdated() = fire { onPointsByDifficultyUpdated(state.userPointsForCategories) }

    private fun fire(event: ExercisesUpdaterListener.() -> Unit) {
        application.invokeLater {
            project.messageBus
                .syncPublisher(EXERCISES_TOPIC)
                .event()
        }
    }

    interface ExercisesUpdaterListener {
        @RequiresEdt
        fun onExercisesUpdated()

        @RequiresEdt
        fun onExerciseUpdated(exercise: Exercise)

        @RequiresEdt
        fun onPointsByDifficultyUpdated(pointsByDifficulty: Map<String, Int>?)
    }

    companion object {
        @ProjectLevel
        val EXERCISES_TOPIC: Topic<ExercisesUpdaterListener> =
            Topic(ExercisesUpdaterListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        private val GRADING_POLL_INTERVAL = 5.seconds

        fun getInstance(project: Project): ExercisesUpdater {
            return project.service<ExercisesUpdater>()
        }


        @NonNls
        private const val FEEDBACK_STRING = "|en:Feedback|"

        /** The result of [computeCategoryPoints]: the per-category totals used to render points-by-difficulty. */
        internal data class CategoryPoints(
            val userPointsForCategories: Map<String, Int>,
            val maxPointsForCategories: Map<String, Int>,
            /** The course-wide totals, shown instead when there are no difficulty categories. */
            val userPointsTotal: Int,
            val maxPointsTotal: Int
        )

        /**
         * Sums the max points per (non-empty) difficulty category across all modules, and looks up the
         * user's points for each of those categories (0 if the category is absent from
         * [APlusApi.Course.Points.PointsBody.pointsByDifficulty]).
         */
        internal fun computeCategoryPoints(points: APlusApi.Course.Points.PointsBody): CategoryPoints {
            val maxPointsForCategories = points.modules
                .flatMap { it.exercises }
                .filter { it.difficulty.isNotEmpty() } // Filter out empty categories
                .groupingBy { it.difficulty }
                .fold(0) { sum, exercise -> sum + exercise.maxPoints }
            val userPointsForCategories = maxPointsForCategories.keys.associateWith {
                points.pointsByDifficulty.getOrDefault(it, 0)
            } // If points are 0, the category is not in pointsByDifficulty
            return CategoryPoints(
                userPointsForCategories,
                maxPointsForCategories,
                userPointsTotal = points.points,
                maxPointsTotal = points.modules.sumOf { it.maxPoints }
            )
        }

        /** The result of [computeChangeSnapshot]: the freshly computed totals, and whether they differ from before. */
        internal data class ChangeSnapshot(
            val newPoints: Int,
            val newSubmissionCount: Int,
            val changed: Boolean
        )

        /**
         * Decide whether the exercise tree needs to be rebuilt. Unchanged when there is
         * already an exercise tree ([hasExerciseGroups]) and both totals match the previous refresh.
         */
        internal fun computeChangeSnapshot(
            points: APlusApi.Course.Points.PointsBody,
            previousPoints: Int,
            previousSubmissionCount: Int,
            hasExerciseGroups: Boolean
        ): ChangeSnapshot {
            val exercises = points.modules.flatMap { it.exercises }
            val newSubmissionCount = exercises.sumOf { it.submissions.size }
            val newPoints = exercises.sumOf { it.points }
            val changed = !(
                    hasExerciseGroups &&
                            previousPoints == newPoints &&
                            previousSubmissionCount == newSubmissionCount
                    )
            return ChangeSnapshot(newPoints, newSubmissionCount, changed)
        }

        /**
         * Assembles the exercise tree from the raw API responses.
         */
        internal fun assembleExerciseGroups(
            points: APlusApi.Course.Points.PointsBody,
            exerciseDetails: List<APlusApi.Course.Exercises.CourseModule>,
            submissionData: List<APlusApi.Course.SubmissionData.SubmissionDataBody>,
            language: String,
            optionalCategories: List<String>,
            exerciseModules: Map<Long, Map<String, Module>>
        ): List<ExerciseGroup> {
            val submissionDataById = submissionData.associateBy { it.submissionId }
            val submitterIdsForGroupSubmissions = submissionData
                .groupBy { it.submissionId }
                .filterValues { it.size > 1 }
                .mapValues { (_, entries) -> entries.map { it.userId } }

            val hasCategories = points.modules.flatMap { it.exercises }.any { it.difficulty.isNotEmpty() }
            val optionalCategoriesWithDefault = if (hasCategories) optionalCategories + "" else optionalCategories

            fun buildExercise(
                exercise: APlusApi.Course.Points.Exercise,
                details: APlusApi.Course.Exercises.Exercise?
            ): Exercise {
                val isSubmittable = details?.hasSubmittableFiles ?: false
                return Exercise(
                    id = exercise.id,
                    name = APlusLocalizationUtil.getLocalizedName(exercise.name, language),
                    module = exerciseModules[exercise.id]?.get(language),
                    htmlUrl = details?.htmlUrl ?: "",
                    url = exercise.url,
                    submissionResults = exercise.submissionsWithPoints.map { submission ->
                        val data = submissionDataById[submission.id]
                        SubmissionResult(
                            id = submission.id,
                            url = submission.url,
                            maxPoints = exercise.maxPoints,
                            userPoints = data?.grade ?: submission.grade,
                            latePenalty = data?.penalty,
                            status = SubmissionResult.statusFromString(data?.status),
                            filesInfo = emptyList(),
                            isSubmittable = isSubmittable,
                            submitters = submitterIdsForGroupSubmissions[submission.id],
                        )
                    }.toMutableList(),
                    maxPoints = exercise.maxPoints,
                    userPoints = exercise.points,
                    maxSubmissions = details?.maxSubmissions ?: 0,
                    bestSubmissionId = exercise.bestSubmission?.split("/")?.last()?.toLongOrNull(),
                    difficulty = exercise.difficulty,
                    isSubmittable = isSubmittable,
                    isOptional = optionalCategoriesWithDefault.contains(exercise.difficulty),
                    isFeedback = exercise.name.contains(FEEDBACK_STRING) && exercise.difficulty == ""
                )
            }

            return points.modules.map { module ->
                val details = exerciseDetails.find { it.id == module.id }
                ExerciseGroup(
                    module.id,
                    APlusLocalizationUtil.getLocalizedName(module.name, language),
                    module.maxPoints,
                    module.points,
                    details?.htmlUrl ?: "",
                    details?.isOpen == true,
                    details?.closingTime,
                    exercises = module.exercises.map { exercise ->
                        buildExercise(exercise, details?.exercises?.find { it.id == exercise.id })
                    }.toMutableList()
                )
            }
        }
    }
}
