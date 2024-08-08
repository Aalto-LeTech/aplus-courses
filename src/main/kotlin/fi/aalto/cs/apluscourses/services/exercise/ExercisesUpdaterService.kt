package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.services.TokenStorage
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.notifications.FeedbackAvailableNotification
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil
import io.ktor.client.statement.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
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
    //: SimplePersistentStateComponent<ExercisesUpdaterService.State>(State()) {
    class State { // : BaseState() {
        //        @get:Attribute(converter = ExerciseGroup.Companion.EGLConverter::class)
//        @set:Attribute(converter = ExerciseGroup.Companion.EGLConverter::class)
        var exerciseGroups: MutableList<ExerciseGroup> = mutableListOf()//by list()
        var userPointsForCategories: Map<String, Int>? = null
        var maxPointsForCategories: Map<String, Int>? = null
        fun increment() = {}//incrementModificationCount()
        fun clearAll() {
            exerciseGroups.clear()
            userPointsForCategories = null
            maxPointsForCategories = null
        }
    }

    val state = State()
    //        val exercisesTree: ExercisesTree by property(exercisesTree)
//        @get:Property(surroundWithTag = false)
//        @get:XCollection(style = XCollection.Style.v2)
//        @get:Attribute(converter = EGConverter::class)
//    @OptIn(DelicateCoroutinesApi::class)
//    private val requestThreadPool = newFixedThreadPoolContext(8, "ExercisesUpdaterService")


//    private val client = HttpClient(CIO) {
//        install(HttpCache) {
//            val cacheFile = Files.createDirectories(Paths.get(".idea/aplusCourses/.http-cache")).toFile()
//            privateStorage(FileStorage(cacheFile))
//        }
//        engine {
//            endpoint {
//                maxConnectionsCount = 8
//            }
//        }
//    }

    // Semaphore to limit the number of concurrent requests
    val semaphore = Semaphore(8) // Up to 8 concurrent operations
//    private val threadContext = newSingleThreadContext("ExercisesUpdaterService")

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
//        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    /**
     * Construct an updater with the given parameters.
     */
//constructor(
//    private val courseProject: CourseProject,
//    private val eventToTrigger: Event,
//    private val notifier: Notifier = DefaultNotifier(),
//    updateInterval: Long = PluginSettings.UPDATE_INTERVAL
//) : RepeatedTask(courseProject.project, updateInterval) {
    private var exerciseJob: Job? = null
    private var gradingJob: Job? = null
    private val submissionsInGrading: MutableSet<Long> = ConcurrentHashMap.newKeySet()
    private var submissionCount = -1
    private var points = -1

    fun restart(
//        courseProject: CourseProject
    ) {
        exerciseJob?.cancel(CancellationException("test"))
        gradingJob?.cancel(CancellationException("test"))
        println("restart")
        runExerciseUpdater(
//            courseProject
        )
        runGradingUpdater()
//        run()
    }

    fun stop() {
        exerciseJob?.cancel(CancellationException("test"))
        gradingJob?.cancel(CancellationException("test"))
    }

    private fun runExerciseUpdater(
//        courseProject: CourseProject,
        updateInterval: Long = 300000
    ) {
        exerciseJob =
            cs.launch {
                try {
                    while (true) {
                        doTask()
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
                        doGradingTask()
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

    object SubmissionDetails {
        @Serializable
        data class Submission(
//            val id: Long,
//            val url: String,
//            val submissionTime: String,
//            val grade: Int,
            val status: String,
            val latePenaltyApplied: Double? = null,
//            val exercise: Long,
//            val user: String,
//            val files: List<File>
        )

        @Serializable
        data class File(
            val id: Long,
            val url: String,
            val filename: String,
            val size: Int,
            val mimeType: String
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun doTask(
//        courseProject: CourseProject,
//        notifier: Notifier = DefaultNotifier(),
    ) {
        if (!TokenStorage.getInstance().isTokenSet()) {
            println("Not authenticated")
            state.clearAll()
            fireExercisesUpdated()
            return
        }
        val course = CourseManager.course(project) ?: return
        println("Starting exercises update")
        val timeStart = System.currentTimeMillis()
//        logger.debug("Starting exercises update")
//        val course = courseProject.course
//        val dataSource = course.exerciseDataSource
//        val authentication = courseProject.authentication
//        val hiddenElements = course.hiddenElements
//        if (authentication == null) {
//            if (courseProject.exerciseTree != null) {
//                courseProject.exerciseTree = ExercisesTree()
////                eventToTrigger.trigger()
//                fireExercisesUpdated()
//            }
////            logger.warn("Not authenticated")
//            return
//        }
//        val progressViewModel =
//            PluginSettings.getInstance().getMainViewModel(courseProject.project).progressViewModel
        val selectedLanguage = CourseFileManager.getInstance(project).state.language!!
//            .getCourseFileManager(courseProject.project).language
//        val progress =
//            progressViewModel.start(
//                3,
//                PluginResourceBundle.getText("ui.ProgressBarView.refreshingAssignments"),
//                false
//            )
//        try {
//            progress.increment()


        cs.ensureActive()
        val apiUrl = "https://plus.cs.aalto.fi/api/v2/"
//        val courseUrl = "${apiUrl}courses/154/" // 2020
        val courseUrl = "${apiUrl}courses/294/" // 2023

        val exercisesUrl = "${courseUrl}exercises/"
        val pointsUrl = "${courseUrl}points/me"
        val submissionDataUrl = "${courseUrl}submissiondata/me?best=no&format=json"
        val coursesClient = project.service<CoursesClient>()
        val response = coursesClient.get(pointsUrl, token = true) // TODO refactor
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
        state.increment()
        firePointsByDifficultyUpdated()
//        val newSubmissionCount = points.submissionCount // TODO https://github.com/apluslms/a-plus/issues/1384
//        val newPoints = points.points
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
        data class SubmissionData(val SubmissionID: Long, val UserID: Long, val Status: String, val Penalty: Double?)

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
            ExerciseGroup(
                module.id,
                APlusLocalizationUtil.getLocalizedName(module.name, selectedLanguage),
                module.maxPoints,
                module.points,
                exerciseModule?.htmlUrl ?: "",
                exerciseModule?.isOpen == true,
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
        state.increment()
        fireExercisesUpdated()
        val newSubmissionsInGrading = newExerciseGroups
            .flatMap { it.exercises }
            .flatMap { it.submissionResults }
            .filter { it.status == SubmissionResult.Status.WAITING }
            .map { it.id }
        submissionsInGrading.clear()
        submissionsInGrading.addAll(newSubmissionsInGrading)
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
                            state.increment()
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
