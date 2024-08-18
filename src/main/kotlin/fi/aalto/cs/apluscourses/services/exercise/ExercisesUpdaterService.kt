package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.notifications.FeedbackAvailableNotification
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.TokenStorage
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil
import io.ktor.client.statement.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import java.util.concurrent.ConcurrentHashMap


@Service(Service.Level.PROJECT)
@State(
    name = "ExercisesUpdaterService",
    storages = [Storage("aplusCoursesExercisesUpdater.xml")]
)
class ExercisesUpdaterService(
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

    val state = State()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true // TODO remove after hasSubmittableFiles is fixed
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json2 = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true // TODO remove after hasSubmittableFiles is fixed
    }

    private var exerciseJob: Job? = null
    private var gradingJob: Job? = null
    private val submissionsInGrading: MutableSet<Long> = ConcurrentHashMap.newKeySet()
    private var submissionCount = -1
    private var points = -1

    fun restart() {
        exerciseJob?.cancel(CancellationException("test"))
        gradingJob?.cancel(CancellationException("test"))
        println("restart")
        runExerciseUpdater()
        runGradingUpdater()
    }

    fun stop() {
        exerciseJob?.cancel(CancellationException("test"))
        gradingJob?.cancel(CancellationException("test"))
    }

    private fun runExerciseUpdater(
        updateInterval: Long = 300_000
    ) {
        exerciseJob =
            cs.launch {
                try {
                    while (true) {
                        withBackgroundProgress(project, "A+ Courses", cancellable = true) {
                            reportSequentialProgress { reporter ->
                                reporter.indeterminateStep("Refreshing assingments")
                                doTask()
                            }
                        }
                        cs.ensureActive()
                        delay(updateInterval)
                    }
                } catch (e: CancellationException) {
                    println("Task was cancelled 1")
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
                        withBackgroundProgress(project, "A+ Courses", cancellable = true) {
                            reportProgress { reporter ->
                                reporter.indeterminateStep("Assignment in grading") {
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
                println("Task was cancelled")
            }
        }
    }

    object Points {
        @Serializable
        data class PointsDataHolder(
            val id: Long,
            val url: String,
            val username: String,
            val studentId: String,
            val email: String,
            val fullName: String,
            val isExternal: Boolean,
            val tags: List<Tag>,
            val role: String,
            val submissionCount: Int,
            val points: Int,
            val pointsByDifficulty: Map<String, Int>,
            val modules: List<Module>
        )

        @Serializable
        data class Tag(
            val id: Long,
            val url: String,
            val slug: String,
            val name: String
        )

        @Serializable
        data class Module(
            val id: Long,
            val name: String,
            val maxPoints: Int,
            val pointsToPass: Int,
            val submissionCount: Int,
            val points: Int,
            val pointsByDifficulty: Map<String, Int>,
            val passed: Boolean,
            val exercises: List<Exercise>
        )

        @Serializable
        data class Exercise(
            val url: String,
            val bestSubmission: String?,
            val submissions: List<String>,
            val submissionsWithPoints: List<SubmissionWithPoints>,
            val id: Long,
            val name: String,
            val difficulty: String,
            val maxPoints: Int,
            val pointsToPass: Int,
            val submissionCount: Int,
            val points: Int,
            val passed: Boolean,
            val official: Boolean
        )

        @Serializable
        data class SubmissionWithPoints(
            val id: Long,
            val url: String,
            val submissionTime: String,
            val grade: Int
        )
    }

    object Exercises {
        @Serializable
        data class CourseModuleResults(
            val results: List<CourseModule>
        )

        @Serializable
        data class CourseModule(
            val id: Long,
            val url: String,
            val htmlUrl: String,
            val displayName: String,
            val isOpen: Boolean,
            val closingTime: String,
            val exercises: List<Exercise>
        )

        @Serializable
        data class Exercise(
            val id: Long,
            val url: String,
            val htmlUrl: String,
            val displayName: String,
            val maxPoints: Int,
            val maxSubmissions: Int,
            val hierarchicalName: String,
            val difficulty: String,
//            val hasSubmittableFiles: Boolean // TODO should always be bool, currently boolean or null or array
            val hasSubmittableFiles: Boolean?
        )
    }

    private suspend fun doTask() {
        if (!TokenStorage.getInstance().isTokenSet()) {
            println("Not authenticated")
            state.clearAll()
            fireExercisesUpdated()
            return
        }
        val course = CourseManager.course(project) ?: return
        println("Starting exercises update")
        val timeStart = System.currentTimeMillis()
        val selectedLanguage = CourseFileManager.getInstance(project).state.language!!


        cs.ensureActive()
//        val apiUrl = "https://plus.cs.aalto.fi/api/v2/"
        val apiUrl = "http://localhost:8000/api/v2/"
//        val courseUrl = "${apiUrl}courses/154/" // 2020
//        val courseUrl = "${apiUrl}courses/294/" // 2023
        val courseUrl = "${apiUrl}courses/1/"

        val exercisesUrl = "${courseUrl}exercises/"
        val pointsUrl = "${courseUrl}points/me"
        val submissionDataUrl = "${courseUrl}submissiondata/me?best=no&format=json"
        val coursesClient = project.service<CoursesClient>()
        val response = coursesClient.get(pointsUrl, token = true) // TODO refactor
        println(response.bodyAsText())
        val points = json.decodeFromString<Points.PointsDataHolder>(response.bodyAsText())
        val pointsAndCategories =
            points.modules
                .flatMap { it.exercises }
                .map { it.difficulty to it.maxPoints }
                .filter { !it.first.isEmpty() } // Filter out empty categories
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
        println("c")
        println(userPointsForCategories)
        println(maxPointsForCategories)
        state.userPointsForCategories = userPointsForCategories
        state.maxPointsForCategories = maxPointsForCategories
        firePointsByDifficultyUpdated()
        val newSubmissionCount = points.modules.flatMap { it.exercises }.sumOf { it.submissions.size }
        val newPoints = points.modules.flatMap { it.exercises }.sumOf { it.points }
        if (this.state.exerciseGroups.isNotEmpty() && this.points == newPoints && this.submissionCount == newSubmissionCount) {
            println("No new data")
            return
        }
        this.points = newPoints
        this.submissionCount = newSubmissionCount
        val (exercisesResponse, submissionDataCsv) = runBlocking {
            awaitAll(
                async { coursesClient.get(exercisesUrl, true) },
                async { coursesClient.get(submissionDataUrl, true) }
            )
        }

        @Serializable
        data class SubmissionData(
            val SubmissionID: Long,
            val UserID: Long,
            val Status: String,
            val Penalty: Double?
        )

        val submissionDataParsed = json2.decodeFromString<List<SubmissionData>>(
            submissionDataCsv.bodyAsText()
        )

        val submissionData = submissionDataParsed.associateBy { it.SubmissionID }
        val submissionsWithMultipleSubmitters: Map<Long, List<Long>> = submissionDataParsed
            .groupBy { it.SubmissionID }
            .filter { it.value.size > 1 }
            .map { it.key to it.value.map { it.UserID } }
            .toMap()
        println("done processing submission data ${submissionData.size}")
        println(response.status)

        val tempFixForHasSubmittableFiles = // TODO
            exercisesResponse.bodyAsText()
                .replace("\"has_submittable_files\":[]", "\"has_submittable_files\":false")
        val exercises = json.decodeFromString<Exercises.CourseModuleResults>(tempFixForHasSubmittableFiles)
        println(exercises.results.size)

        println(points.modules.size)
        val newExerciseGroups = points.modules.map { module ->
            val exerciseModule = exercises.results.find { it.id == module.id }
            println("Processing module ${module.name} ${selectedLanguage}")
            ExerciseGroup(
                module.id,
                APlusLocalizationUtil.getLocalizedName(module.name, selectedLanguage),
                module.maxPoints,
                module.points,
                exerciseModule?.htmlUrl ?: "",
                exerciseModule?.isOpen == true,
                exerciseModule?.closingTime,
                exerciseOrder = listOf(),
                exercises = module.exercises.map { exercise ->
                    val exerciseExercise = exerciseModule?.exercises?.find { it.id == exercise.id }
                    Exercise(
                        id = exercise.id,
                        name = APlusLocalizationUtil.getLocalizedName(exercise.name, selectedLanguage),
                        module = course.exerciseModules[exercise.id]?.get(selectedLanguage),
                        htmlUrl = exerciseExercise?.htmlUrl ?: "",
                        url = exercise.url,
                        submissionInfo = null,
                        submissionResults = exercise
                            .submissionsWithPoints
                            .map {
                                val data = submissionData[it.id]
                                val submitters = submissionsWithMultipleSubmitters[it.id]
                                if (submitters != null) {
                                    println("Multiple submitters for ${it.id}: $submitters")
                                }
                                SubmissionResult(
                                    id = it.id,
                                    url = it.url,
                                    maxPoints = exercise.maxPoints,
                                    userPoints = it.grade,
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
                        isOptional = listOf("optional", "training", "").contains(exercise.difficulty),
                        isDetailsLoaded = true
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
            gradingJob?.cancel("Restart grading")
            runGradingUpdater()
        }
        println("done processing exercises")
        val timeEnd = System.currentTimeMillis()
        println("Time taken: ${timeEnd - timeStart} ms")
    }

    private fun doGradingTask() {
        println("Starting grading update")
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
                            submissionResult.status = SubmissionResult.statusFromString(submission.status)
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
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
                .syncPublisher(EXERCISES_TOPIC)
                .onExercisesUpdated()
        }
    }

    private fun fireExerciseUpdated(exercise: Exercise) {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
                .syncPublisher(EXERCISES_TOPIC)
                .onExerciseUpdated(exercise)
        }
    }

    private fun firePointsByDifficultyUpdated() {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
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
//        private val logger: Logger = APlusLogger.logger

        @ProjectLevel
        val EXERCISES_TOPIC: Topic<ExercisesUpdaterListener> =
            Topic(ExercisesUpdaterListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        fun getInstance(project: Project): ExercisesUpdaterService {
            return project.service<ExercisesUpdaterService>()
        }
    }
}
