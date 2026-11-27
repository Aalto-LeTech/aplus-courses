package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.aplus.client.AplusException
import fi.aalto.cs.aplus.client.LocalizedString
import fi.aalto.cs.aplus.client.dto.*
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.notifications.FeedbackAvailableNotification
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.TokenStorage
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
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
            } catch (e: AplusException) {
                handleNetworkError(e)
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
            } catch (e: AplusException) {
                handleGradingNetworkError(e)
            } catch (e: IOException) {
                handleGradingNetworkError(e)
            } catch (e: UnresolvedAddressException) {
                handleGradingNetworkError(e)
            }
        }

    private suspend fun updateExercises(force: Boolean) {
        if (!TokenStorage.getInstance().isTokenSet(project)) {
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
        val points: PointsDto,
        val exercises: List<ExerciseModuleDto>,
        val submissionData: List<SubmissionDataRowDto>
    )

    /**
     * Fetches the data needed to rebuild the exercise tree, or returns null if nothing has
     * changed since the last refresh and [force] is false.
     */
    private suspend fun fetchIfChanged(course: Course, force: Boolean): FetchedExercises? {
        val aplus = CoursesClient.getInstance(project).aplus
        val points = withContext(Dispatchers.IO) { aplus.points(course.id) }
        updateCategoryPoints(points)
        val changed = updateChangeSnapshot(points)
        if (!changed && !force) return null
        return coroutineScope {
            val exercisesDeferred = async(Dispatchers.IO) { aplus.exerciseModules(course.id) }
            val submissionDataDeferred = async(Dispatchers.IO) { aplus.submissionData(course.id) }
            FetchedExercises(points, exercisesDeferred.await(), submissionDataDeferred.await())
        }
    }

    private fun updateCategoryPoints(points: PointsDto) {
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
    private fun updateChangeSnapshot(points: PointsDto): Boolean {
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
        var anyFinished = false
        for (submissionId in submissionsInGrading.toList()) {
            val submission = CoursesClient.getInstance(project).aplus.submission(submissionId)
            val status = SubmissionResult.statusFromString(submission.status)
            if (status == SubmissionResult.Status.WAITING) continue
            submissionsInGrading.remove(submissionId)
            if (status == SubmissionResult.Status.UNKNOWN) {
                CoursesLogger.warn("Submission $submissionId has unknown status ${submission.status}")
            }
            anyFinished = true
            applyGradedSubmission(submissionId, submission)
        }
        // Terminal failures also have to leave the grading state, even when points stay unchanged.
        if (anyFinished) restart(force = true)
    }

    private fun applyGradedSubmission(submissionId: Long, submission: SubmissionDto) {
        val exercise = state.exerciseGroups
            .flatMap { it.exercises }
            .find { it.submissionResults.any { result -> result.id == submissionId } } ?: return
        val submissionResult = exercise.submissionResults.find { it.id == submissionId } ?: return
        submissionResult.status = SubmissionResult.statusFromString(submission.status)
        submissionResult.userPoints = submission.grade
        submissionResult.latePenalty = submission.latePenaltyApplied
        if (submissionResult.status in setOf(
                SubmissionResult.Status.GRADED,
                SubmissionResult.Status.UNOFFICIAL,
                SubmissionResult.Status.REJECTED
            )
        ) {
            Notifier.notify(FeedbackAvailableNotification(submissionResult, exercise, project), project)
        }
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
         * Totals maximum points by nonempty difficulty category across modules.
         * Missing entries in [PointsDto.pointsByDifficulty] count as zero earned points.
         */
        internal fun computeCategoryPoints(points: PointsDto): CategoryPoints {
            val maxPointsForCategories = points.modules
                .flatMap { it.exercises }
                .filter { it.difficulty.isNotEmpty() }
                .groupingBy { it.difficulty }
                .fold(0) { sum, exercise -> sum + exercise.maxPoints }
            val userPointsForCategories = maxPointsForCategories.keys.associateWith {
                points.pointsByDifficulty.getOrDefault(it, 0)
            }
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
            points: PointsDto,
            previousPoints: Int,
            previousSubmissionCount: Int,
            hasExerciseGroups: Boolean
        ): ChangeSnapshot {
            val exercises = points.allExercises()
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
            points: PointsDto,
            exerciseDetails: List<ExerciseModuleDto>,
            submissionData: List<SubmissionDataRowDto>,
            language: String,
            optionalCategories: List<String>,
            exerciseModules: Map<Long, Map<String, Module>>
        ): List<ExerciseGroup> {
            val submissionDataById = submissionData.associateBy { it.submissionId }
            val submitterIdsForGroupSubmissions = submissionData.submitterIdsBySubmission()
                .filterValues { it.size > 1 }

            val hasCategories = points.allExercises().any { it.difficulty.isNotEmpty() }
            val optionalCategoriesWithDefault = if (hasCategories) optionalCategories + "" else optionalCategories

            fun buildExercise(
                exercise: PointsExerciseDto,
                details: ExerciseBriefDto?
            ): Exercise {
                val isSubmittable = details?.hasSubmittableFiles ?: false
                return Exercise(
                    id = exercise.id,
                    name = LocalizedString(exercise.name).pick(language),
                    module = exerciseModules[exercise.id]?.get(language),
                    htmlUrl = details?.htmlUrl.orEmpty(),
                    submissionResults = exercise.submissionsWithPoints.map { submission ->
                        val data = submissionDataById[submission.id]
                        SubmissionResult(
                            id = submission.id,
                            maxPoints = exercise.maxPoints,
                            userPoints = data?.grade ?: submission.grade,
                            latePenalty = data?.penalty,
                            status = SubmissionResult.statusFromString(data?.status),
                            isSubmittable = isSubmittable,
                            submitters = submitterIdsForGroupSubmissions[submission.id],
                        )
                    }.toMutableList(),
                    maxPoints = exercise.maxPoints,
                    userPoints = exercise.points,
                    maxSubmissions = details?.maxSubmissions ?: 0,
                    bestSubmissionId = exercise.resolvedBestSubmissionId,
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
                    LocalizedString(module.name).pick(language),
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
