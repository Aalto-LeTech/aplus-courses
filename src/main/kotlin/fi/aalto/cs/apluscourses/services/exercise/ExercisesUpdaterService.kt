package fi.aalto.cs.apluscourses.services.exercise

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import com.intellij.util.xmlb.annotations.Attribute
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup
import fi.aalto.cs.apluscourses.model.exercise.SubmissionInfo
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil
import io.ktor.client.statement.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
) : SimplePersistentStateComponent<ExercisesUpdaterService.State>(State()) {
    class State : BaseState() {
        @get:Attribute(converter = ExerciseGroup.Companion.EGLConverter::class)
        @set:Attribute(converter = ExerciseGroup.Companion.EGLConverter::class)
        var exerciseGroups: MutableList<ExerciseGroup> by list()
        fun increment() = incrementModificationCount()
    }

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
        namingStrategy = JsonNamingStrategy.SnakeCase
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
    private var job: Job? = null
    private val submissionsInGrading: MutableSet<Long> = ConcurrentHashMap.newKeySet()

    fun restart(
//        courseProject: CourseProject
    ) {
        job?.cancel(CancellationException("test"))
        println("restart")
        run(
//            courseProject
        )
//        run()
    }

    fun stop() {
        job?.cancel(CancellationException("test"))
    }

    private fun run(
//        courseProject: CourseProject,
        updateInterval: Long = 300000
    ) {
        job =
            cs.launch {
                try {
                    while (true) {
                        doTask()
                        cs.ensureActive()
                        delay(updateInterval)
                    }
                } catch (e: CancellationException) {
                    println("Task was cancelled yay")
                }
//                finally {
//                    client.close()
//                }
            }
    }

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
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

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
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
            val difficulty: String
        )
    }

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    object ExerciseDetails {
        @Serializable
        data class Exercise(
            val exerciseInfo: ExerciseInfo? = null
        )

        @Serializable
        data class ExerciseInfo(
            val formSpec: List<FormSpec>? = null,
            val formI18n: Map<String, Map<String, String>>
        )

        @Serializable
        data class FormSpec(
            val type: String,
            val required: Boolean? = null,
            val title: String,
            val key: String
        )
    }

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
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

    private suspend fun doTask(
//        courseProject: CourseProject,
//        notifier: Notifier = DefaultNotifier(),
    ) {
        try {
            project.service<CoursesClient>().updateAuthentication()
            println("Starting exercises update")
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
            val selectedLanguage = "en" //PluginSettings.getInstance()
//            .getCourseFileManager(courseProject.project).language
//        val progress =
//            progressViewModel.start(
//                3,
//                PluginResourceBundle.getText("ui.ProgressBarView.refreshingAssignments"),
//                false
//            )
//        try {
//            progress.increment()


            suspend fun request(url: String) = withContext(Dispatchers.IO) {
                project.service<CoursesClient>().get(url)
            }
            cs.ensureActive()
            val apiUrl = "https://plus.cs.aalto.fi/api/v2/"
            val courseUrl = "${apiUrl}courses/154/"
            val exercisesUrl = "${courseUrl}exercises/"
            val pointsUrl = "${courseUrl}points/me"

            val response = request(pointsUrl)

            println(response.status)

            val exercisesResponse = request(exercisesUrl)

            val exercises = json.decodeFromString<Exercises.CourseModuleResults>(exercisesResponse.bodyAsText())
            println(exercises.results.size)

            val points = json.decodeFromString<Points.PointsDataHolder>(response.bodyAsText())
            println(points.modules.size)
            val exerciseGroups = points.modules.map { module ->
                val exerciseModule = exercises.results.find { it.id == module.id }
                ExerciseGroup(
                    module.id,
                    APlusLocalizationUtil.getLocalizedName(module.name, selectedLanguage),
                    module.maxPoints,
                    module.points,
                    exerciseModule?.htmlUrl ?: "",
                    exerciseModule?.isOpen == true,
                    exerciseOrder = listOf(),
                    exercises =
                    module.exercises.map { exercise ->
                        val exerciseExercise = exerciseModule?.exercises?.find { it.id == exercise.id }
                        Exercise(
                            id = exercise.id,
                            name = APlusLocalizationUtil.getLocalizedName(exercise.name, selectedLanguage),
                            htmlUrl = exerciseExercise?.htmlUrl ?: "",
                            url = exercise.url,
                            submissionInfo = null,
                            submissionResults = exercise
                                .submissionsWithPoints
                                .reversed() // Reverse order for correct indexing
                                .mapIndexed { i, it ->
                                    SubmissionResult(
                                        id = it.id,
                                        url = it.url,
                                        maxPoints = exercise.maxPoints,
                                        userPoints = it.grade,
                                        latePenalty = null,
                                        status = SubmissionResult.Companion.Status.UNKNOWN,
                                        filesInfo = emptyList(),
                                    )
                                }.toMutableList(),
                            maxPoints = exercise.maxPoints,
                            userPoints = exercise.points,
                            maxSubmissions = exerciseExercise?.maxSubmissions ?: 0,
                            bestSubmissionId = exercise.bestSubmission?.split("/")?.last()?.toLong(),
                            difficulty = exercise.difficulty,
                            isSubmittable = exercise.name.contains("("), // TODO get info from api
                            isOptional = listOf("optional", "training", "").contains(exercise.difficulty)
                        )
                    }.toMutableList()
                )
            }
            if (state.exerciseGroups.isEmpty()) {
                state.exerciseGroups.addAll(exerciseGroups)
                state.increment()
                fireExercisesUpdated()
            }
            cs.ensureActive()
//        fireExercisesUpdated()
//        return
            runBlocking {
                for (group in state.exerciseGroups) {
                    val requests = group.exercises.map { exercise ->
                        async {
                            semaphore.withPermit {
                                try {
                                    if (!exercise.isDetailsLoaded && exercise.isSubmittable) {
                                        val exerciseResponse = request(exercise.url)
                                        cs.ensureActive()
                                        println("exerciseResponse: ${exerciseResponse.status}")
                                        val exerciseDetails =
                                            json.decodeFromString<ExerciseDetails.Exercise>(exerciseResponse.bodyAsText())
                                        val submissionInfo = SubmissionInfo.fromJsonObject(exerciseDetails)
                                        exercise.submissionInfo = submissionInfo
                                        state.increment()
                                        cs.ensureActive()
                                        // TODO check if is late
//                                        if (exercise.userPoints != exercise.maxPoints) {
//                                        } else {
//                                            val bestSubmission = exercise.bestSubmission()
//                                            if (bestSubmission != null && !bestSubmission.isDetailsLoaded) {
////                                    checkCancelled()
//                                                val submissionResponse = withContext(Dispatchers.IO) {
//                                                    client.get(bestSubmission.url) {
//                                                        headers {
//                                                            append(
//                                                                "Authorization",
//                                                            )
//                                                        }
//                                                    }
//                                                }
//                                                if (!isActive) {
//                                                    return@async
//                                                }
//                                                println("submissionResponse: ${submissionResponse.status}")
//                                                val submissionDetails =
//                                                    json.decodeFromString<SubmissionDetails.Submission>(
//                                                        submissionResponse.bodyAsText()
//                                                    )
//                                                bestSubmission.updateStatus(submissionDetails.status)
//                                                bestSubmission.latePenalty = submissionDetails.latePenaltyApplied ?: 0.0
//                                                bestSubmission.isDetailsLoaded = true
//                                                state.increment()
//                                            }
//
//                                        }
                                    }
                                    exercise.isDetailsLoaded = true
                                    state.increment()
                                    fireExerciseUpdated(exercise)
                                } catch (e: CancellationException) {
                                    println("Fetching was cancelled")
                                    throw e // Re-throw to propagate cancellation
                                }
                            }
                        }
                    }
                    println("group ${group.name} done")
                    requests.awaitAll()
                    println("group ${group.name} done2")
//                }
                }
//            }
            }
            println("done")
//            fireExercisesUpdated()


            /*
                    var exerciseGroups = dataSource.getExerciseGroups(course, authentication, selectedLanguage)
        //            logger.info("Exercise groups count: {}", exerciseGroups.size)
                    cs.ensureActive()
                    exerciseGroups = exerciseGroups.stream()
                        .filter { group: ExerciseGroup ->
                            !hiddenElements.shouldHideObject(
                                group.id,
                                group.name,
                                selectedLanguage
                            )
                        }
                        .collect(Collectors.toList())
                    progress.increment()
                    val selectedStudent = courseProject.selectedStudent
        //            logger.info("Selected student: {}", selectedStudent)
                    val exerciseTree = ExercisesTree(exerciseGroups, selectedStudent)
                    if (courseProject.exerciseTree == null) {
                        courseProject.exerciseTree = exerciseTree
        //                eventToTrigger.trigger()
                        fireExercisesUpdated()
                    }
                    val points = dataSource.getPoints(course, authentication, selectedStudent)

                    addDummySubmissionResults(exerciseGroups, points)
                    cs.ensureActive()

                    progress.incrementMaxValue(
                        submissionsToBeLoadedCount(courseProject, exerciseGroups, points)
                                + exercisesToBeLoadedCount(courseProject, exerciseGroups)
                    )
                    addExercises(courseProject, exerciseGroups, points, authentication, progress, selectedLanguage)

                    cs.ensureActive()

        //            eventToTrigger.trigger()
                    fireExercisesUpdated()
        //            logger.debug("Exercises update done")
                } catch (e: IOException) {
        //            logger.warn("Network error during exercise update", e)
                    notifier.notify(NetworkErrorNotification(e), courseProject.project)
                } finally {
                    progress.finish()
                }
            }


            @Throws(IOException::class)
            private fun addExercises(
                courseProject: CourseProject,
                exerciseGroups: List<ExerciseGroup>,
                points: Points,
                authentication: Authentication,
                progress: Progress,
                selectedLanguage: String
            ) {
                val course = courseProject.course
                val dataSource = course.exerciseDataSource
                val selectedStudent = courseProject.selectedStudent
                val exercisesTree = ExercisesTree(exerciseGroups, selectedStudent)
                val hiddenElements = course.hiddenElements
                courseProject.exerciseTree = exercisesTree

                for (exerciseGroup in exerciseGroups) {
                    if (courseProject.isLazyLoadedGroup(exerciseGroup.id)) {
                        for (exerciseId in points.getExercises(exerciseGroup.id)) {
                            if (Thread.interrupted()) {
                                return
                            }
                            val exercise = dataSource.getExercise(
                                exerciseId, points, course.optionalCategories,
                                authentication, CachePreferences.GET_MAX_ONE_WEEK_OLD, selectedLanguage
                            )
                            if (!hiddenElements.shouldHideObject(exercise.id, exercise.name, selectedLanguage)) {
                                exerciseGroup.addExercise(exercise)
                            }

                            progress.increment()

                            addSubmissionResults(courseProject, exercise, points, authentication, progress)

        //                    eventToTrigger.trigger()
                            fireExercisesUpdated()
                        }
                    }
                }
            }

            private fun addDummySubmissionResults(
                exerciseGroups: List<ExerciseGroup>,
                points: Points
            ) {
                for (exerciseGroup in exerciseGroups) {
                    if (Thread.interrupted()) {
                        return
                    }
                    for (exercise in exerciseGroup.exercises) {
                        val submissionIds = points.getSubmissions(exercise.id)
                        for (id in submissionIds) {
                            exercise.addSubmissionResult(DummySubmissionResult(id, exercise))
                        }
                    }
                }
            }

            @Throws(IOException::class)
            private fun addSubmissionResults(
                courseProject: CourseProject,
                exercise: Exercise,
                points: Points,
                authentication: Authentication,
                progress: Progress
            ) {
                val dataSource = courseProject.course.exerciseDataSource
                val baseUrl = courseProject.course.apiUrl + "submissions/"
                val submissionIds = points.getSubmissions(exercise.id)
                for (id in submissionIds) {
                    if (Thread.interrupted()) {
                        return
                    }

                    // Ignore cache for submissions that had the status WAITING
                    val cachePreference = if (submissionsInGrading.remove(id)
                    ) CachePreferences.GET_NEW_AND_KEEP
                    else CachePreferences.GET_MAX_ONE_WEEK_OLD
                    val submission = dataSource.getSubmissionResult(
                        "$baseUrl$id/", exercise, authentication, courseProject.course, cachePreference
                    )
                    if (submission.status == SubmissionResult.Status.WAITING) {
                        submissionsInGrading.add(id)
                    }

                    exercise.addSubmissionResult(submission)
                    progress.increment()
                }
            }

            private fun submissionsToBeLoadedCount(
                courseProject: CourseProject,
                exerciseGroups: List<ExerciseGroup>,
                points: Points
            ): Int {
                return exerciseGroups.stream()
                    .filter { group: ExerciseGroup -> courseProject.isLazyLoadedGroup(group.id) }
                    .mapToInt { group: ExerciseGroup ->
                        group.exercises.stream()
                            .mapToInt { exercise: Exercise -> points.getSubmissionsAmount(exercise.id) }.sum()
                    }
                    .sum()
            }

            private fun exercisesToBeLoadedCount(courseProject: CourseProject, exerciseGroups: List<ExerciseGroup>): Int {
                return exerciseGroups.stream()
                    .filter { group: ExerciseGroup -> courseProject.isLazyLoadedGroup(group.id) }
                    .mapToInt { group: ExerciseGroup -> group.exercises.size }
                    .sum()
            }*/
        } catch (_: CancellationException) {
        }
        //        finally {
//            client.close()
//        }
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

    interface ExercisesUpdaterListener {
        @RequiresEdt
        fun onExercisesUpdated()

        @RequiresEdt
        fun onExerciseUpdated(exercise: Exercise)
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
