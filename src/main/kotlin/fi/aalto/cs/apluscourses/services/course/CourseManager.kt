package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.news.NewsList
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.notifications.NewModulesVersionsNotification
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.TokenStorage
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.apluscourses.utils.Version
import fi.aalto.cs.apluscourses.utils.callbacks.Callbacks
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@Service(Service.Level.PROJECT)
class CourseManager(
    private val project: Project,
    val cs: CoroutineScope
) {
    class State {
        var authenticated: Boolean? = null
        var course: Course? = null
        var courseName: String? = null
        var news: NewsList? = null
        var user: User? = null
        var feedbackCss: String? = null
        var aPlusUrl: String? = null
        var grading: CourseConfig.Grading? = null
        var alwaysShowGroups = false
        var settingsImported = false
        var error: Error? = null
        var missingDependencies = mapOf<String, List<Component<*>>>()
        fun clearAll() {
            course = null
            news = null
            user = null
            feedbackCss = null
            settingsImported = false
            missingDependencies = emptyMap()
        }
    }

    enum class Error {
        NOT_ENROLLED,
        NETWORK_ERROR,
    }

    val state = State()

    private val moduleOperationDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val notifiedModules: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var job: Job? = null
    fun restart() {
        job?.cancel()
        run()
    }

    private fun run(
        updateInterval: Long = PluginSettings.UPDATE_INTERVAL
    ) {
        job =
            cs.launch {
                try {
                    while (true) {
                        withBackgroundProgress(project, message("aplusCourses")) {
                            reportSequentialProgress { reporter ->
                                reporter.indeterminateStep(message("services.progress.refreshingCourse"))
                                doTask()
                            }
                        }
                        cs.ensureActive()
                        delay(updateInterval)
                    }
                } catch (_: CancellationException) {

                } catch (e: Exception) {
                    when (e) {
                        is IOException, is UnresolvedAddressException -> getInstance(project)
                            .fireNetworkError()

                        else -> throw e
                    }
                }
            }
    }

    fun fireNetworkError() {
        state.error = Error.NETWORK_ERROR
        state.clearAll()
        fireCourseUpdated()
    }

    fun setNewsAsRead() {
        val news = state.news ?: return
        news.setAllRead()
        CourseFileManager.getInstance(project).setNewsRead()
        fireNewsUpdated()
    }

    private suspend fun doTask() {
        project.service<CourseFileManager>().migrateOldConfig()
        val courseConfig = CourseConfig.get(project)
        if (courseConfig == null) {
            fireNetworkError()
            return
        }

        state.courseName = courseConfig.name
        state.aPlusUrl = courseConfig.aPlusUrl
        if (!TokenStorage.getInstance().isTokenSet()) {
            state.clearAll()
            state.authenticated = false
            fireCourseUpdated()
            return
        }
        state.authenticated = true
        state.error = null

        state.grading = courseConfig.grading
        state.alwaysShowGroups = courseConfig.alwaysShowGroups

        val extraCourseData = try {
            state.user = withContext(Dispatchers.IO) {
                APlusApi.me().get(project)
            }
            APlusApi.Course(courseConfig.id.toLong()).get(project)
        } catch (_: Exception) {
            val courseId = courseConfig.id.toLong()
            val user = state.user
            if (user != null && (!user.isStaffOf(courseId) || !user.isEnrolledIn(courseId))) {
                state.error = Error.NOT_ENROLLED
            } else {
                state.error = Error.NETWORK_ERROR
            }
            fireCourseUpdated()
            throw CancellationException()
        }
        val modules = courseConfig.modules.map {
            Module(
                it.name,
                it.url,
                it.changelog,
                it.version,
                it.language,
                project
            )
        }
        val exerciseModules = courseConfig.exerciseModules.map { (exerciseId, languagesToModule) ->
            exerciseId to
                    languagesToModule
                        .map { (language, moduleName) ->
                            var module = modules.find { it.name == moduleName }
                            if (module == null) {
                                module = Module(
                                    moduleName,
                                    "",
                                    "",
                                    Version.EMPTY,
                                    null,
                                    project
                                )
                            }
                            language to module
                        }
                        .toMap()
        }.toMap()
        state.course = Course(
            id = courseConfig.id.toLong(),
            name = courseConfig.name,
            htmlUrl = extraCourseData.htmlUrl,
            imageUrl = extraCourseData.image,
            endingTime = extraCourseData.endingTime,
            languages = courseConfig.languages,
            modules = modules,
            exerciseModules = exerciseModules,
            resourceUrls = CourseConfig.resourceUrls(courseConfig.resources),
            optionalCategories = courseConfig.optionalCategories,
            autoInstallComponentNames = courseConfig.autoInstall,
            replInitialCommands = courseConfig.scalaRepl?.initialCommands,
            replAdditionalArguments = courseConfig.scalaRepl?.arguments,
            minimumPluginVersion = courseConfig.version,
            hiddenElements = courseConfig.hiddenElements,
            callbacks = Callbacks.fromJsonObject(courseConfig.callbacks),
            project
        )
        importSettings(state.course!!)
        state.course?.components?.values?.forEach { withContext(moduleOperationDispatcher) { it.updateStatus() } }

        val course = state.course ?: return

        ExercisesUpdater.getInstance(project).restart()
        val newNews = APlusApi.Course(course.id).news(project)
        state.news?.news?.forEach {
            if (it.isRead) newNews.setRead(it.id)
        }

        state.news = newNews
        fireNewsUpdated()
        fireCourseUpdated()

        val autoInstallModulesToInstall = course.autoInstallComponents
            .filter {
                withContext(moduleOperationDispatcher) {
                    it.updateAndGetStatus() == Component.Status.NOT_LOADED
                }
            }
        if (autoInstallModulesToInstall.isNotEmpty()) {
            autoInstallModulesToInstall.forEach {
                if (it is Module) installModuleAsync(it, false)
            }
        } else {
            refreshModuleStatusesAsync()
        }

        notifyUpdatableModules()
    }

    private suspend fun importSettings(course: Course) {
        if (state.settingsImported) {
            return
        }
        val settingsImporter = project.service<SettingsImporter>()
        state.feedbackCss = settingsImporter.importFeedbackCss(course)
        state.settingsImported = true
    }


    private fun notifyUpdatableModules() {
        val course = state.course ?: return
        val metadata = CourseFileManager.getInstance(project).state.modules
        metadata.map { it.name to it.version }.toMap()
        val updatableModules = course.modules
            .filter { m: Module -> m.isUpdateAvailable && !m.isMinorUpdate }
            .filter { m: Module -> notifiedModules.add(m.name) }
        if (updatableModules.isNotEmpty()) {
            val notification = NewModulesVersionsNotification(updatableModules)
            Notifier.notifyAndHide(notification, project)
        }
    }

    suspend fun refreshModuleStatusesAsync() {
        state.missingDependencies = state.course?.modules?.mapNotNull {
            withContext(moduleOperationDispatcher) {
                it.updateStatus()
            }
            val dependencies = getMissingDependencies(it)
            if (dependencies.isNotEmpty()) {
                it.setError()
                it.name to dependencies
            } else {
                null
            }
        }?.toMap() ?: emptyMap()
        fireModulesUpdated()
    }

    fun refreshModuleStatuses() {
        cs.launch {
            refreshModuleStatusesAsync()
        }
    }

    suspend fun installModuleAsync(module: Module, show: Boolean = true) {
        withBackgroundProgress(project, message("aplusCourses")) {
            reportSequentialProgress { reporter ->
                reporter.indeterminateStep(message("services.progress.installing", module.name))
                CoursesLogger.info("Installing ${module.name}")
                withContext(moduleOperationDispatcher) {
                    module.downloadAndInstall()
                }
                state.course?.callbacks?.invokePostDownloadModuleCallbacks(project, module)

                fireModulesUpdated()
                if (show) project.service<Opener>().showModuleInProjectTree(module)
                val dependencies = getMissingDependencies(module)

                dependencies.forEach {
                    withContext(moduleOperationDispatcher) {
                        it.downloadAndInstall()
                    }
                }
                if (dependencies.isNotEmpty()) {
                    CoursesLogger.info("Installed ${module.name} and its dependencies ${dependencies.map { it.name }}")
                } else {
                    CoursesLogger.info("Installed ${module.name}")
                }
                refreshModuleStatuses()
            }

        }
    }

    fun installModule(module: Module, show: Boolean = true) {
        cs.launch {
            installModuleAsync(module, show)
        }
    }

    fun updateModule(module: Module) {
        cs.launch {
            CoursesLogger.info("Updating ${module.name}")
            withContext(moduleOperationDispatcher) {
                module.update()
            }
            refreshModuleStatusesAsync()
        }
    }

    private suspend fun getMissingDependencies(module: Module): List<Component<*>> {
        return module.dependencyNames
            ?.mapNotNull { state.course?.getComponentIfExists(it) }
            ?.filter { module ->
                val status = withContext(moduleOperationDispatcher) {
                    module.updateAndGetStatus()
                }
                status == Component.Status.NOT_LOADED || status == Component.Status.ERROR
            }
            ?: emptyList()
    }


    private fun fireNewsUpdated() {
        application.invokeLater {
            project.messageBus
                .syncPublisher(NEWS_TOPIC)
                .onNewsUpdated(state.news!!)
        }
    }

    private fun fireModulesUpdated() {
        application.invokeLater {
            project.messageBus
                .syncPublisher(MODULES_TOPIC)
                .onModulesUpdated(state.course)
        }
    }

    private fun fireCourseUpdated() {
        application.invokeLater {
            project.messageBus
                .syncPublisher(COURSE_TOPIC)
                .onCourseUpdated(state.course)
        }
    }

    interface NewsUpdaterListener {
        @RequiresEdt
        fun onNewsUpdated(newsList: NewsList)
    }

    interface ModuleListener {
        fun onModulesUpdated(course: Course?)
    }

    interface CourseListener {
        fun onCourseUpdated(course: Course?)
    }

    companion object {
        @ProjectLevel
        val NEWS_TOPIC: Topic<NewsUpdaterListener> =
            Topic(NewsUpdaterListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        val MODULES_TOPIC: Topic<ModuleListener> =
            Topic(ModuleListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        val COURSE_TOPIC: Topic<CourseListener> =
            Topic(CourseListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        fun getInstance(project: Project): CourseManager {
            return project.service<CourseManager>()
        }

        fun course(project: Project): Course? {
            return getInstance(project).state.course
        }

        fun isCourseEnded(project: Project): Boolean {
            val endingTime = getInstance(project).state.course?.endingTime ?: return false
            val parsedTime = Instant.parse(endingTime)
            val now = Clock.System.now()
            return now > parsedTime
        }

        fun error(project: Project): Error? {
            return getInstance(project).state.error
        }

        fun user(project: Project): User? {
            return getInstance(project).state.user
        }

        fun authenticated(project: Project): Boolean? {
            return getInstance(project).state.authenticated
        }
    }
}
