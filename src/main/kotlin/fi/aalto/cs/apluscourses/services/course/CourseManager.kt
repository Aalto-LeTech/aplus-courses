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
import fi.aalto.cs.apluscourses.activities.InitializationActivity
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.component.SbtModule
import fi.aalto.cs.apluscourses.model.news.NewsList
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.notifications.MissingDependencyNotification
import fi.aalto.cs.apluscourses.notifications.NewModulesVersionsNotification
import fi.aalto.cs.apluscourses.services.*
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.apluscourses.utils.Version
import fi.aalto.cs.apluscourses.utils.callbacks.Callbacks
import kotlinx.coroutines.*
import kotlin.time.Clock
import kotlin.time.Instant
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds

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
        var alwaysShowGroups: Boolean = false
        var settingsImported: Boolean = false
        var error: Error? = null
        var networkErrorMessage: String? = null
        var missingDependencies: Map<String, List<Component<*>>> = mapOf()

        /** Dependency names already reported, so the notification is not repeated every refresh. */
        val notifiedMissingDependencies: MutableSet<String> = ConcurrentHashMap.newKeySet()

        fun clearAll() {
            course = null
            news = null
            user = null
            feedbackCss = null
            settingsImported = false
            missingDependencies = emptyMap()
            notifiedMissingDependencies.clear()
        }
    }

    enum class Error {
        NOT_ENROLLED,
        INVALID_TOKEN,
        NETWORK_ERROR,
    }

    val state: State = State()

    private val moduleOperationDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val notifiedModules: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var job: Job? = null

    private val firstRefreshDone = CompletableDeferred<Unit>()

    suspend fun awaitFirstRefresh(): Unit = firstRefreshDone.await()

    fun restart() {
        state.error = null
        job?.cancel()
        run()
    }

    private fun run(
        updateInterval: Long = PluginSettings.UPDATE_INTERVAL
    ) {
        job =
            cs.launch {
                while (true) {
                    try {
                        withBackgroundProgress(project, message("services.progress.refreshingCourse")) {
                            doTask()
                        }
                    } catch (_: CancellationException) {
                        ensureActive()
                    } catch (e: IOException) {
                        handleRefreshNetworkError(e)
                    } catch (e: UnresolvedAddressException) {
                        handleRefreshNetworkError(e)
                    } finally {
                        firstRefreshDone.complete(Unit)
                    }
                    delay(updateInterval.milliseconds)
                }
            }
    }

    private fun handleRefreshNetworkError(e: Exception) {
        CoursesLogger.warn("Network error in CourseManager", e)
        fireNetworkError(e)
    }

    /** Re-runs the startup initialization that a network failure aborted. */
    fun retryInitialization() {
        if (!initializationRetryInFlight.compareAndSet(false, true)) return
        InitializationStatus.clearIsIoError(project)
        state.error = null
        cs.launch {
            try {
                InitializationActivity().execute(project)
            } finally {
                initializationRetryInFlight.set(false)
            }
        }
    }

    private val initializationRetryInFlight = AtomicBoolean(false)

    fun fireNetworkError(e: Exception? = null) {
        state.error = Error.NETWORK_ERROR
        state.networkErrorMessage = e?.message
        fireCourseUpdated()
    }

    fun setNewsAsRead() {
        val news = state.news ?: return
        news.setAllRead()
        CourseFileManager.getInstance(project).setNewsRead()
        fireNewsUpdated()
    }

    private fun checkIsEnrolled(courseId: Long?, user: User?) {
        if (courseId != null && user != null) {
            if (!user.isStaffOf(courseId) && !user.isEnrolledIn(courseId)) {
                state.error = Error.NOT_ENROLLED
                state.clearAll()
                fireCourseUpdated()
                throw CancellationException()
            }
        }
    }

    private suspend fun doTask(): Unit = reportSequentialProgress { reporter ->
        reporter.nextStep(CONFIG_LOADED, message("services.progress.loadingCourse"))
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
        state.networkErrorMessage = null

        state.grading = courseConfig.grading
        state.alwaysShowGroups = courseConfig.alwaysShowGroups
        reporter.nextStep(CONTENT_LOADED, message("services.progress.loadingContent"))

        val modules = courseConfig.modules.map {
            if (it.sbt) {
                SbtModule(it.name, it.url, it.changelog, it.version, it.language, project)
            } else {
                Module(it.name, it.url, it.changelog, it.version, it.language, project)
            }
        }
        val exerciseModules = courseConfig.exerciseModules.map { (exerciseId, languagesToModule) ->
            exerciseId to
                    languagesToModule
                        .map { (language, moduleName) ->
                            val module = modules.find { it.name == moduleName }
                                ?: Module(moduleName, "", "", Version.EMPTY, null, project)
                            language to module
                        }
                        .toMap()
        }.toMap()
        val course = Course(
            id = courseConfig.id.toLong(),
            name = courseConfig.name,
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

        val previousCourse = state.course
        state.course = course
        withContext(moduleOperationDispatcher) {
            course.components.values.forEach { it.updateStatus() }
        }
        fireModulesUpdated()

        val courseId = courseConfig.id.toLong()
        val fetched = try {
            coroutineScope {
                val user = async(Dispatchers.IO) { APlusApi.me().get(project) }
                val courseData = async(Dispatchers.IO) { APlusApi.Course(courseId).get(project) }
                val news = async(Dispatchers.IO) { APlusApi.Course(courseId).news(project) }

                state.user = user.await()
                courseData.await() to news.await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            state.course = previousCourse
            if (e is ForbiddenException) {
                CoursesLogger.info("A+ refused course $courseId, so this student is not on it")
                state.error = Error.NOT_ENROLLED
                state.clearAll()
                fireCourseUpdated()
                throw CancellationException()
            }

            checkIsEnrolled(courseId, state.user)

            if (e is UnauthorizedException) {
                CoursesLogger.warn("A+ rejected the token", e)
                state.error = Error.INVALID_TOKEN
            } else {
                CoursesLogger.warn("Error while fetching user or course", e)
                state.error = Error.NETWORK_ERROR
                state.networkErrorMessage = e.message
            }
            fireCourseUpdated()
            throw CancellationException()
        }
        val extraCourseData = fetched.first
        val fetchedNews = fetched.second

        checkIsEnrolled(courseConfig.id.toLong(), state.user)
        course.htmlUrl = extraCourseData.htmlUrl
        course.imageUrl = extraCourseData.image
        course.endingTime = extraCourseData.endingTime

        importSettings(course)

        ExercisesUpdater.getInstance(project).restart()
        state.news?.news?.forEach {
            if (it.isRead) fetchedNews.setRead(it.id)
        }
        state.news = fetchedNews
        fireNewsUpdated()
        fireCourseUpdated()

        val autoInstallModulesToInstall = course.autoInstallComponents
            .filter {
                withContext(moduleOperationDispatcher) {
                    it.updateAndGetStatus() == Component.Status.NOT_LOADED
                }
            }
        if (autoInstallModulesToInstall.isNotEmpty()) {
            val modules = autoInstallModulesToInstall.filterIsInstance<Module>()
            reporter.nextStep(REFRESH_FINISHED) {
                reportSequentialProgress(modules.size) { moduleReporter ->
                    modules.forEach { module ->
                        moduleReporter.sizedStep(
                            workSize = 1,
                            text = message("services.progress.installing", module.name)
                        ) {
                            installModuleAsync(module, show = false, ownTask = false)
                        }
                    }
                }
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
        metadata.associate { it.name to it.version }
        val updatableModules = course.modules
            .filter { m: Module -> m.isUpdateAvailable && !m.isMinorUpdate }
            .filter { m: Module -> notifiedModules.add(m.name) }
        if (updatableModules.isNotEmpty()) {
            val notification = NewModulesVersionsNotification(updatableModules)
            Notifier.notifyAndHide(notification, project)
        }
    }

    private suspend fun refreshModuleStatusesAsync() {
        state.missingDependencies = withContext(moduleOperationDispatcher) {
            state.course?.modules?.mapNotNull { module ->
                module.updateStatus()
                val dependencies = missingDependenciesOf(module)
                if (dependencies.isNotEmpty()) {
                    module.setError()
                    module.name to dependencies
                } else {
                    null
                }
            }?.toMap() ?: emptyMap()
        }
        notifyOfUnresolvedDependencies()
        fireModulesUpdated()
    }

    private fun unresolvedDependencyNames(module: Module): Set<String> {
        val course = state.course ?: return emptySet()
        return module.dependencyNames
            .orEmpty()
            .filterTo(mutableSetOf()) { course.getComponentIfExists(it) == null }
    }

    private fun notifyOfUnresolvedDependencies() {
        val course = state.course ?: return
        val unresolved = course.modules
            .flatMapTo(sortedSetOf()) { unresolvedDependencyNames(it) }
            .filterTo(sortedSetOf()) { state.notifiedMissingDependencies.add(it) }
        if (unresolved.isNotEmpty()) {
            CoursesLogger.warn("Unresolved module dependencies: ${unresolved.joinToString(", ")}")
            Notifier.notify(MissingDependencyNotification(unresolved), project)
        }
    }

    private val moduleStatusRefreshJob = AtomicReference<Job?>(null)

    fun refreshModuleStatuses() {
        val sweep = cs.launch {
            delay(MODULE_STATUS_REFRESH_DEBOUNCE)
            refreshModuleStatusesAsync()
        }
        moduleStatusRefreshJob.getAndSet(sweep)?.cancel()
    }

    /**
     * @param ownTask whether to open a background task named after the module. Off when the
     *   caller already reports progress that this install belongs inside, as during a course
     *   refresh.
     */
    private suspend fun installModuleAsync(
        module: Module,
        show: Boolean = true,
        ownTask: Boolean = true
    ) {
        if (ownTask) {
            withBackgroundProgress(project, message("services.progress.installing", module.name)) {
                installModuleSteps(module, show)
            }
        } else {
            installModuleSteps(module, show)
        }
    }

    private suspend fun installModuleSteps(module: Module, show: Boolean) {
        module.setLoading()
        fireModuleStatusChanged(module)
        try {
            reportSequentialProgress { reporter ->
                reporter.nextStep(endFraction = DOWNLOAD_DONE) {
                    CoursesLogger.info("Installing ${module.name}")
                    withContext(moduleOperationDispatcher) {
                        module.downloadAndInstall()
                    }
                }

                reporter.nextStep(CALLBACKS_DONE) {
                    state.course?.callbacks?.invokePostDownloadModuleCallbacks(project, module)
                    fireModulesUpdated()
                    if (show) project.service<Opener>().showModuleInProjectTree(module)
                }

                val dependencies = getMissingDependencies(module)
                reporter.nextStep(DEPENDENCIES_DONE) {
                    if (dependencies.isNotEmpty()) {
                        reportSequentialProgress(dependencies.size) { dependencyReporter ->
                            dependencies.forEach { dependency ->
                                dependencyReporter.sizedStep(
                                    workSize = 1,
                                    text = message("services.progress.installing", dependency.name)
                                ) {
                                    withContext(moduleOperationDispatcher) {
                                        dependency.downloadAndInstall()
                                    }
                                }
                            }
                        }
                    }
                }

                if (dependencies.isNotEmpty()) {
                    CoursesLogger.info(
                        "Installed ${module.name} dependencies ${dependencies.map { it.name }}"
                    )
                }

                reporter.nextStep(FINISHED) {
                    refreshModuleStatuses()
                }
            }
        } catch (e: CancellationException) {
            module.clearLoading()
            fireModuleStatusChanged(module)
            throw e
        } catch (e: Exception) {
            module.clearLoading()
            fireModuleStatusChanged(module)
            refreshModuleStatuses()
            throw e
        }
    }

    fun installModule(module: Module, show: Boolean = true) {
        module.setLoading()
        fireModuleStatusChanged(module)
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

    suspend fun getMissingDependencies(module: Module): List<Component<*>> =
        withContext(moduleOperationDispatcher) { missingDependenciesOf(module) }

    /** Must already be running on [moduleOperationDispatcher]. */
    private fun missingDependenciesOf(module: Module): List<Component<*>> =
        module.dependencyNames
            ?.mapNotNull { state.course?.getComponentIfExists(it) }
            ?.filter { dependency ->
                val status = dependency.updateAndGetStatus()
                status == Component.Status.NOT_LOADED || status == Component.Status.ERROR
            }
            ?: emptyList()


    private fun fireNewsUpdated() {
        application.invokeLater {
            val news = state.news ?: return@invokeLater
            project.messageBus
                .syncPublisher(NEWS_TOPIC)
                .onNewsUpdated(news)
        }
    }

    private fun fireModulesUpdated() {
        application.invokeLater {
            project.messageBus
                .syncPublisher(MODULES_TOPIC)
                .onModulesUpdated(state.course)
        }
    }

    private fun fireModuleStatusChanged(module: Module) {
        application.invokeLater {
            project.messageBus
                .syncPublisher(MODULES_TOPIC)
                .onModuleStatusChanged(module)
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
        fun onModuleStatusChanged(module: Module)
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

        /** How the refresh progress bar is divided between the phases of a refresh. */
        private const val CONFIG_LOADED = 5
        private const val CONTENT_LOADED = 15
        private const val REFRESH_FINISHED = 100

        /**
         * How the install progress bar is divided between the phases of installing a module.
         *
         * The download takes much longer than the phases after it, so it owns nearly all the bar.
         */
        private const val DOWNLOAD_DONE = 88
        private const val CALLBACKS_DONE = 90
        private const val DEPENDENCIES_DONE = 98
        private const val FINISHED = 100

        /** Bursts of installs within this window share a single module status sweep. */
        private val MODULE_STATUS_REFRESH_DEBOUNCE = 300.milliseconds
    }
}
