package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.APlusApi
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


@Service(Service.Level.PROJECT)
@State(
    name = "ExercisesUpdaterService",
    storages = [Storage("aplusCoursesExercisesUpdater.xml")]
)
class ExercisesUpdater(
    val project: Project,
    val cs: CoroutineScope
) {
    class State {
        var exerciseGroups: MutableList<ExerciseGroup> = mutableListOf()
        var userPointsForCategories: Map<String, Int>? = null
        var maxPointsForCategories: Map<String, Int>? = null
        fun clearAll() {
            exerciseGroups.clear()
            userPointsForCategories = null
            maxPointsForCategories = null
        }
    }

    val state: State = State()

    @NonNls
    private val feedbackString = "|en:Feedback|"

    private var exerciseJob: Job? = null
    private var gradingJob: Job? = null
    var isRunning: Boolean = false
        private set
    private val submissionsInGrading: MutableSet<Long> = ConcurrentHashMap.newKeySet()
    private var submissionCount = -1
    private var points = -1

    fun restart() {
        exerciseJob?.cancel()
        gradingJob?.cancel()
        runExerciseUpdater()
        runGradingUpdater()
    }

    private fun runExerciseUpdater() {
        exerciseJob =
            cs.launch {
                try {
                    withBackgroundProgress(project, message("aplusCourses"), cancellable = true) {
                        reportSequentialProgress { reporter ->
                            reporter.indeterminateStep(message("services.progress.refreshingAssignments"))
                            doTask()
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is CancellationException -> {}

                        is IOException, is UnresolvedAddressException -> {
                            CoursesLogger.error("Network error in ExercisesUpdater", e)
                            state.clearAll()
                            CourseManager.getInstance(project)
                                .fireNetworkError()
                        }

                        else -> throw e
                    }
                }
            }
    }

    private fun runGradingUpdater(
        updateInterval: Long = 5000
    ) {
        gradingJob = cs.launch {
            try {
                while (true) {
                    if (submissionsInGrading.isNotEmpty()) {
                        withBackgroundProgress(project, message("aplusCourses"), cancellable = true) {
                            reportProgress { reporter ->
                                reporter.indeterminateStep(message("services.progress.gradingAssignments")) {
                                    while (submissionsInGrading.isNotEmpty()) {
                                        doGradingTask()
                                        cs.ensureActive()
                                        delay(updateInterval)
                                    }
                                }
                            }
                        }
                    }
                    cs.ensureActive()
                    delay(updateInterval)
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    private suspend fun doTask() {
        if (!TokenStorage.getInstance().isTokenSet()) {
            CoursesLogger.info("Not authenticated, clearing exercises")
            state.clearAll()
            fireExercisesUpdated()
            return
        }
        val course = CourseManager.course(project) ?: return
        isRunning = true
        CoursesLogger.info("Updating exercises for course ${course.id}")
        val timeStart = System.currentTimeMillis()
        val selectedLanguage = CourseFileManager.getInstance(project).state.language!!


        cs.ensureActive()
        val courseApi = APlusApi.course(course)

        val points = courseApi.points(project)
        val pointsAndCategories =
            points.modules
                .flatMap { it.exercises }
                .map { it.difficulty to it.maxPoints }
                .filter { it.first.isNotEmpty() } // Filter out empty categories
        val userPointsForCategories = pointsAndCategories
            .map { it.first }
            .toSet()
            .associateWith {
                points.pointsByDifficulty.getOrDefault(it, 0)
            } // If points are 0, the category is not in pointsByDifficulty
        val maxPointsForCategories: Map<String, Int> =
            pointsAndCategories.fold(mutableMapOf()) { acc: MutableMap<String, Int>, pair: Pair<String, Int> ->
                val (category, points) = pair
                acc[category] = acc.getOrDefault(category, 0) + points
                acc
            }

        state.userPointsForCategories = userPointsForCategories
        state.maxPointsForCategories = maxPointsForCategories
        firePointsByDifficultyUpdated()
        val newSubmissionCount = points.modules.flatMap { it.exercises }.sumOf { it.submissions.size }
        val newPoints = points.modules.flatMap { it.exercises }.sumOf { it.points }
        if (this.state.exerciseGroups.isNotEmpty() && this.points == newPoints && this.submissionCount == newSubmissionCount) {
            CoursesLogger.info("No changes in exercises")
            isRunning = false
            return
        }
        this.points = newPoints
        this.submissionCount = newSubmissionCount
        val exercisesResponseDeferred = withContext(Dispatchers.IO) { async { courseApi.exercises(project) } }
        val submissionDataResponseDeferred = withContext(Dispatchers.IO) { async { courseApi.submissionData(project) } }

        val exercises = exercisesResponseDeferred.await()
        val submissionDataResponse = submissionDataResponseDeferred.await()

        val submissionData = submissionDataResponse.associateBy { it.SubmissionID }
        val submissionsWithMultipleSubmitters: Map<Long, List<Long>> = submissionDataResponse
            .groupBy { it.SubmissionID }
            .filter { it.value.size > 1 }
            .map { (submissionId, submitters) -> submissionId to submitters.map { it.UserID } }
            .toMap()

        val optionalCategories = course.optionalCategories + "" // Empty category counted as optional

        val newExerciseGroups = points.modules.map { module ->
            val exerciseModule = exercises.find { it.id == module.id }
            ExerciseGroup(
                module.id,
                APlusLocalizationUtil.getLocalizedName(module.name, selectedLanguage),
                module.maxPoints,
                module.points,
                exerciseModule?.htmlUrl ?: "",
                exerciseModule?.isOpen == true,
                exerciseModule?.closingTime,
                exercises = module.exercises.map { exercise ->
                    val exerciseExercise = exerciseModule?.exercises?.find { it.id == exercise.id }
                    Exercise(
                        id = exercise.id,
                        name = APlusLocalizationUtil.getLocalizedName(exercise.name, selectedLanguage),
                        module = course.exerciseModules[exercise.id]?.get(selectedLanguage),
                        htmlUrl = exerciseExercise?.htmlUrl ?: "",
                        url = exercise.url,
                        submissionResults = exercise
                            .submissionsWithPoints
                            .map {
                                val data = submissionData[it.id]
                                val submitters = submissionsWithMultipleSubmitters[it.id]
                                SubmissionResult(
                                    id = it.id,
                                    url = it.url,
                                    maxPoints = exercise.maxPoints,
                                    userPoints = data?.Grade ?: it.grade,
                                    latePenalty = data?.Penalty,
                                    status = SubmissionResult.statusFromString(data?.Status),
                                    filesInfo = emptyList(),
                                    submitters = submitters,
                                )
                            }.toMutableList(),
                        maxPoints = exercise.maxPoints,
                        userPoints = exercise.points,
                        maxSubmissions = exerciseExercise?.maxSubmissions ?: 0,
                        bestSubmissionId = exercise.bestSubmission?.split("/")?.last()?.toLong(),
                        difficulty = exercise.difficulty,
                        isSubmittable = exerciseExercise?.hasSubmittableFiles == true,
                        isOptional = optionalCategories.contains(exercise.difficulty),
                        isFeedback = exercise.name.contains(feedbackString) && exercise.difficulty == ""
                    )
                }.toMutableList()
            )
        }
        state.exerciseGroups.clear()
        state.exerciseGroups.addAll(newExerciseGroups)
        fireExercisesUpdated()
        val newSubmissionsInGrading = newExerciseGroups
            .flatMap { it.exercises }
            .flatMap { it.submissionResults }
            .filter { it.status == SubmissionResult.Status.WAITING }
            .map { it.id }
        submissionsInGrading.clear()
        submissionsInGrading.addAll(newSubmissionsInGrading)
        if (submissionsInGrading.isNotEmpty()) {
            gradingJob?.cancel()
            runGradingUpdater()
        }
        val timeEnd = System.currentTimeMillis()
        CoursesLogger.info("Done updating exercises. Time taken: ${timeEnd - timeStart} ms")
        isRunning = false
    }

    private fun doGradingTask() {
        val submissions = submissionsInGrading.toList()
        var anyPassed = false
        runBlocking {
            for (submissionId in submissions) {
                val submission = APlusApi.Submission(submissionId).get(project)
                if (SubmissionResult.statusFromString(submission.status) != SubmissionResult.Status.WAITING) {
                    anyPassed = true
                    submissionsInGrading.remove(submissionId)
                    val exercise = state.exerciseGroups
                        .flatMap { it.exercises }
                        .find { it.id == submission.exercise.id }
                    if (exercise != null) {
                        val submissionResult = exercise.submissionResults.find { it.id == submissionId }
                        if (submissionResult != null) {
                            submissionResult.updateStatus(submission.status)
                            submissionResult.userPoints = submission.grade
                            submissionResult.latePenalty = submission.latePenaltyApplied
                            Notifier.notify(FeedbackAvailableNotification(submissionResult, exercise, project), project)
                            fireExerciseUpdated(exercise)
                        }
                    }
                }
            }
            if (anyPassed) {
                restart()
            }
        }
    }

    private fun fireExercisesUpdated() {
        application.invokeLater {
            project.messageBus
                .syncPublisher(EXERCISES_TOPIC)
                .onExercisesUpdated()
        }
    }

    private fun fireExerciseUpdated(exercise: Exercise) {
        application.invokeLater {
            project.messageBus
                .syncPublisher(EXERCISES_TOPIC)
                .onExerciseUpdated(exercise)
        }
    }

    private fun firePointsByDifficultyUpdated() {
        application.invokeLater {
            project.messageBus
                .syncPublisher(EXERCISES_TOPIC)
                .onPointsByDifficultyUpdated(state.userPointsForCategories)
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

        fun getInstance(project: Project): ExercisesUpdater {
            return project.service<ExercisesUpdater>()
        }
    }
}
