package fi.aalto.cs.apluscourses.model
//
//import com.intellij.openapi.application.ApplicationManager
//import com.intellij.openapi.components.Service
//import com.intellij.openapi.project.Project
//import com.intellij.util.concurrency.annotations.RequiresEdt
//import com.intellij.util.messages.Topic
//import com.intellij.util.messages.Topic.ProjectLevel
//import fi.aalto.cs.apluscourses.notifications.DefaultNotifier
//import fi.aalto.cs.apluscourses.notifications.NetworkErrorNotification
//import fi.aalto.cs.apluscourses.intellij.notifications.Notifier
//import fi.aalto.cs.apluscourses.services.PluginSettings
//import fi.aalto.cs.apluscourses.model.*
//import fi.aalto.cs.apluscourses.utils.APlusLogger
//import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
//import fi.aalto.cs.apluscourses.utils.cache.CachePreferences
//import io.ktor.utils.io.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import org.slf4j.Logger
//import java.io.IOException
//import java.util.concurrent.ConcurrentHashMap
//import java.util.stream.Collectors
//
//@Service(Service.Level.PROJECT)
//class ExercisesUpdaterService(
//    private val project: Project,
//    val cs: CoroutineScope
//) {
//    /**
//     * Construct an updater with the given parameters.
//     */
////constructor(
////    private val courseProject: CourseProject,
////    private val eventToTrigger: Event,
////    private val notifier: Notifier = DefaultNotifier(),
////    updateInterval: Long = PluginSettings.UPDATE_INTERVAL
////) : RepeatedTask(courseProject.project, updateInterval) {
//    private var job: Job? = null
//    private val submissionsInGrading: MutableSet<Long> = ConcurrentHashMap.newKeySet()
//
//    fun restart(courseProject: CourseProject) {
//        job?.cancel(CancellationException("test"))
//        println("restart")
//        run(courseProject)
////        run()
//    }
//
//    fun stop() {
//        job?.cancel(CancellationException("test"))
//    }
//
//    private fun run(courseProject: CourseProject, updateInterval: Long = PluginSettings.UPDATE_INTERVAL) {
//        job =
//            cs.launch {
//                while (true) {
//                    doTask(courseProject)
//                    delay(updateInterval)
//                }
//            }
//    }
//
//    fun doTask(
//        courseProject: CourseProject,
//        notifier: Notifier = DefaultNotifier(),
//    ) {
//        println("Starting exercises update")
////        logger.debug("Starting exercises update")
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
//        val selectedLanguage = PluginSettings.getInstance()
//            .getCourseFileManager(courseProject.project).language
//        val progress =
//            progressViewModel.start(
//                3,
//                PluginResourceBundle.getText("ui.ProgressBarView.refreshingAssignments"),
//                false
//            )
//        try {
//            progress.increment()
//            if (Thread.interrupted()) {
//                return
//            }
//            var exerciseGroups = dataSource.getExerciseGroups(course, authentication, selectedLanguage)
////            logger.info("Exercise groups count: {}", exerciseGroups.size)
//            if (Thread.interrupted()) {
//                return
//            }
//            exerciseGroups = exerciseGroups.stream()
//                .filter { group: ExerciseGroup ->
//                    !hiddenElements.shouldHideObject(
//                        group.id,
//                        group.name,
//                        selectedLanguage
//                    )
//                }
//                .collect(Collectors.toList())
//            progress.increment()
//            val selectedStudent = courseProject.selectedStudent
////            logger.info("Selected student: {}", selectedStudent)
//            val exerciseTree = ExercisesTree(exerciseGroups, selectedStudent)
//            if (courseProject.exerciseTree == null) {
//                courseProject.exerciseTree = exerciseTree
////                eventToTrigger.trigger()
//                fireExercisesUpdated()
//            }
//            val points = dataSource.getPoints(course, authentication, selectedStudent)
//
//            addDummySubmissionResults(exerciseGroups, points)
//            if (Thread.interrupted()) {
//                return
//            }
//
//            progress.incrementMaxValue(
//                submissionsToBeLoadedCount(courseProject, exerciseGroups, points)
//                        + exercisesToBeLoadedCount(courseProject, exerciseGroups)
//            )
//            addExercises(courseProject, exerciseGroups, points, authentication, progress, selectedLanguage)
//
//            if (Thread.interrupted()) {
//                return
//            }
//
////            eventToTrigger.trigger()
//            fireExercisesUpdated()
////            logger.debug("Exercises update done")
//        } catch (e: IOException) {
////            logger.warn("Network error during exercise update", e)
//            notifier.notify(NetworkErrorNotification(e), courseProject.project)
//        } finally {
//            progress.finish()
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun addExercises(
//        courseProject: CourseProject,
//        exerciseGroups: List<ExerciseGroup>,
//        points: Points,
//        authentication: Authentication,
//        progress: Progress,
//        selectedLanguage: String
//    ) {
//        val course = courseProject.course
//        val dataSource = course.exerciseDataSource
//        val selectedStudent = courseProject.selectedStudent
//        val exercisesTree = ExercisesTree(exerciseGroups, selectedStudent)
//        val hiddenElements = course.hiddenElements
//        courseProject.exerciseTree = exercisesTree
//
//        for (exerciseGroup in exerciseGroups) {
//            if (courseProject.isLazyLoadedGroup(exerciseGroup.id)) {
//                for (exerciseId in points.getExercises(exerciseGroup.id)) {
//                    if (Thread.interrupted()) {
//                        return
//                    }
//                    val exercise = dataSource.getExercise(
//                        exerciseId, points, course.optionalCategories,
//                        authentication, CachePreferences.GET_MAX_ONE_WEEK_OLD, selectedLanguage
//                    )
//                    if (!hiddenElements.shouldHideObject(exercise.id, exercise.name, selectedLanguage)) {
//                        exerciseGroup.addExercise(exercise)
//                    }
//
//                    progress.increment()
//
//                    addSubmissionResults(courseProject, exercise, points, authentication, progress)
//
////                    eventToTrigger.trigger()
//                    fireExercisesUpdated()
//                }
//            }
//        }
//    }
//
//    private fun addDummySubmissionResults(
//        exerciseGroups: List<ExerciseGroup>,
//        points: Points
//    ) {
//        for (exerciseGroup in exerciseGroups) {
//            if (Thread.interrupted()) {
//                return
//            }
//            for (exercise in exerciseGroup.exercises) {
//                val submissionIds = points.getSubmissions(exercise.id)
//                for (id in submissionIds) {
//                    exercise.addSubmissionResult(DummySubmissionResult(id, exercise))
//                }
//            }
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun addSubmissionResults(
//        courseProject: CourseProject,
//        exercise: Exercise,
//        points: Points,
//        authentication: Authentication,
//        progress: Progress
//    ) {
//        val dataSource = courseProject.course.exerciseDataSource
//        val baseUrl = courseProject.course.apiUrl + "submissions/"
//        val submissionIds = points.getSubmissions(exercise.id)
//        for (id in submissionIds) {
//            if (Thread.interrupted()) {
//                return
//            }
//
//            // Ignore cache for submissions that had the status WAITING
//            val cachePreference = if (submissionsInGrading.remove(id)
//            ) CachePreferences.GET_NEW_AND_KEEP
//            else CachePreferences.GET_MAX_ONE_WEEK_OLD
//            val submission = dataSource.getSubmissionResult(
//                "$baseUrl$id/", exercise, authentication, courseProject.course, cachePreference
//            )
//            if (submission.status == SubmissionResult.Status.WAITING) {
//                submissionsInGrading.add(id)
//            }
//
//            exercise.addSubmissionResult(submission)
//            progress.increment()
//        }
//    }
//
//    private fun submissionsToBeLoadedCount(
//        courseProject: CourseProject,
//        exerciseGroups: List<ExerciseGroup>,
//        points: Points
//    ): Int {
//        return exerciseGroups.stream()
//            .filter { group: ExerciseGroup -> courseProject.isLazyLoadedGroup(group.id) }
//            .mapToInt { group: ExerciseGroup ->
//                group.exercises.stream()
//                    .mapToInt { exercise: Exercise -> points.getSubmissionsAmount(exercise.id) }.sum()
//            }
//            .sum()
//    }
//
//    private fun exercisesToBeLoadedCount(courseProject: CourseProject, exerciseGroups: List<ExerciseGroup>): Int {
//        return exerciseGroups.stream()
//            .filter { group: ExerciseGroup -> courseProject.isLazyLoadedGroup(group.id) }
//            .mapToInt { group: ExerciseGroup -> group.exercises.size }
//            .sum()
//    }
//
//    private fun fireExercisesUpdated() {
//        ApplicationManager.getApplication().invokeLater {
//            ApplicationManager.getApplication().messageBus
//                .syncPublisher(TOPIC)
//                .onExercisesUpdated()
//        }
//    }
//
//    interface ExercisesUpdaterListener {
//        @RequiresEdt
//        fun onExercisesUpdated()
//    }
//
//    companion object {
//        private val logger: Logger = APlusLogger.logger
//
//        @ProjectLevel
//        val TOPIC: Topic<ExercisesUpdaterListener> =
//            Topic(ExercisesUpdaterListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)
//
//        fun getInstance(project: Project): ExercisesUpdaterService {
//            return project.getService(ExercisesUpdaterService::class.java)
//        }
//    }
//}
