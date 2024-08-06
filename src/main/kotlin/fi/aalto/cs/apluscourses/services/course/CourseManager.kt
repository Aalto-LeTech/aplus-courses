package fi.aalto.cs.apluscourses.services.course

import com.intellij.notification.Notification
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.dal.TokenStorage
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.news.NewsTree
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.notifications.NewModulesVersionsNotification
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.utils.Version
import fi.aalto.cs.apluscourses.utils.callbacks.Callbacks
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class CourseManager(
    private val project: Project,
    val cs: CoroutineScope
) {
    //: SimplePersistentStateComponent<CourseManager.State>(State()) {
    class State {
        //: BaseState() {
        var course: Course? = null
        var news: NewsTree? = null
        var user: User? = null
        var feedbackCss: String? = null
        var settingsImported = false
        var missingDependencies = mapOf<String, List<Component<*>>>()
    }

    val state = State()

    private val notifiedModules: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var moduleUpdatesNotification: Notification? = null

    //    /**
//     * Construct a course updater with reasonable defaults.
//     */
//    constructor(
//        courseProject: CourseProject,
//        course: Course,
//        project: Project,
//        courseUrl: URL,
//        eventToTrigger: Event
//    ) : this(
//        courseProject, course, project, courseUrl, eventToTrigger, DefaultNotifier(),
//        PluginSettings.UPDATE_INTERVAL
//    )
    private var job: Job? = null
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
        updateInterval: Long = 300000 // TODO PluginSettings.UPDATE_INTERVAL
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

    fun setNewsAsRead() {
        val news = state.news ?: return
        news.setAllRead()
        fireNewsUpdated(news)
    }

    private suspend fun doTask() {
        project.service<CourseFileManager>().migrateOldConfig()
        if (!TokenStorage.isTokenSet()) {
            return
        }
//        CoursesClient.getInstance().updateAuthentication()


//        val progressViewModel =
//            PluginSettings.getInstance().getMainViewModel(project).progressViewModel
//        val progress =
//            progressViewModel.start(3, PluginResourceBundle.getText("ui.ProgressBarView.refreshingCourse"), false)
//        val selectedLanguage = PluginSettings
//            .getInstance()
//            .getCourseFileManager(project).language

//        val auth = courseProject.authentication
        try {
            runBlocking {
                async {
                    println(CourseFileManager.getInstance(project).state.url)
                    val courseConfig = CoursesClient.getInstance(project)
                        .getBody<CourseConfig.JSON>(
                            CourseFileManager.getInstance(project).state.url!!,
                            false
                        )
                    val extraCourseData = APlusApi.Course(courseConfig.id.toLong()).get(project)
                    val modules = courseConfig.modules.map {
                        Module(
                            it.name,
                            it.url,
                            it.changelog,
                            it.version,
                            project
                        )
                    }
                    val exerciseModules = courseConfig.exerciseModules.map { (exerciseId, languagesToModule) ->
                        println("exerciseId: $exerciseId languagesToModule: $languagesToModule")
                        exerciseId to
                                languagesToModule
                                    .map { (language, moduleName) ->
                                        var module = modules.find { it.name == moduleName }
                                        if (module == null) {
                                            println("Module $moduleName not found")
                                            module = Module(
                                                moduleName,
                                                "",
                                                "",
                                                Version.EMPTY,
                                                project
                                            )
                                        }
                                        language to module
                                    }
                                    .toMap()
                    }.toMap()
                    state.course = Course(
                        courseConfig.id.toLong(),
                        courseConfig.name,
                        courseConfig.aPlusUrl,
                        extraCourseData.htmlUrl,
                        extraCourseData.image,
                        extraCourseData.endingTime,
                        courseConfig.languages,
                        modules,
                        exerciseModules,
                        CourseConfig.resourceUrls(courseConfig.resources),
                        courseConfig.vmOptions,
                        courseConfig.optionalCategories,
                        courseConfig.autoInstall,
                        courseConfig.scalaRepl?.initialCommands,
                        courseConfig.scalaRepl?.arguments,
                        courseConfig.version,
                        courseConfig.hiddenElements,
                        Callbacks.fromJsonObject(courseConfig.callbacks),
                        project
                    )
                    importSettings(state.course!!)
                    state.course?.components?.values?.forEach { it.load() }
                }
                async {
                    state.user = withContext(Dispatchers.IO) {
                        APlusApi.me().get(project)
                    }
                }
            }
            val course = state.course ?: return
            course.autoInstallComponents.forEach {
                val status = it.loadAndGetStatus()
                if (status == Component.Status.UNRESOLVED && it is Module) {
                    installModule(it, false)
                }
            }
            fireCourseUpdated()
            ExercisesUpdaterService.getInstance(project).restart()
            refreshModuleStatuses()
            val newNews = runBlocking {
//                APlusApi.Course(course.id).news().get()
                APlusApi.Course(294).news().get(project)

            }
            state.news?.news?.forEach {
                if (it.isRead) newNews.setRead(it.id)
            }

            state.news = newNews
            fireNewsUpdated(newNews)
        } catch (e: IOException) {
//            progress.finish()
            return
        }

        notifyUpdatableModules()
    }

    private suspend fun importSettings(course: Course) {
        if (state.settingsImported) {
            return
        }
        val settingsImporter = project.service<SettingsImporter>()
//                settingsImporter.importCustomProperties(Paths.get(project.basePath!!), course, project)
        state.feedbackCss = settingsImporter.importFeedbackCss(course)
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

    fun refreshModuleStatuses() {
        state.missingDependencies = state.course?.modules?.mapNotNull {
            it.load()
            val dependencies = getMissingDependencies(it)
            println("module: ${it.name} dependencies: $dependencies")
            if (dependencies.isNotEmpty()) {
                it.setError()
                it.name to dependencies
            } else {
                null
            }
        }?.toMap() ?: emptyMap()
        fireModulesUpdated()
    }

    fun installModule(module: Module, show: Boolean = true) {
        cs.launch {
            withBackgroundProgress(project, "Installing module ${module.name}") {
                reportSequentialProgress { reporter ->
                    reporter.indeterminateStep("Downloading...")
                    module.downloadAndInstall()
                    println("Module installed")
//            refreshModuleStatuses()
                    fireModulesUpdated()
                    if (show) project.service<Opener>().showModuleInProjectTree(module)
//            return@launch
                    val dependencies = getMissingDependencies(module)
                    println("module: ${module.name} dependencies: $dependencies")
                    dependencies.forEach { it.downloadAndInstall() }
                    refreshModuleStatuses()
                }

            }

        }
    }

    private fun getMissingDependencies(module: Module): List<Component<*>> {
        return module.dependencyNames
            ?.mapNotNull { state.course?.getComponentIfExists(it) }
            ?.filter { module ->
                val status = module.loadAndGetStatus()
                status == Component.Status.UNRESOLVED || status == Component.Status.ERROR
            }
            ?: emptyList()
    }

//    private fun getAndLoadDependencies(module: Module, filterStatus: Component.Status?): List<Component<*>> {
//        return module.dependencyNames
//            ?.mapNotNull { state.course?.getComponentIfExists(it) }
//            ?.filter { filterStatus == null || it.status == filterStatus }
//            ?.map { it.load(); it }
//            ?.filter { filterStatus == null || it.status == filterStatus }
//            ?: emptyList()
//    }

    private class ModuleInfo(
        val version: Version,
        val changelog: String
    )


    private fun fireNewsUpdated(newsTree: NewsTree) {
        println("fireNewsUpdated ${newsTree}")
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
                .syncPublisher(NEWS_TOPIC)
                .onNewsUpdated(state.news!!)
        }
    }

    private fun fireModulesUpdated() {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
                .syncPublisher(MODULES_TOPIC)
                .onModulesUpdated(state.course)
        }
    }

    private fun fireCourseUpdated() {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().messageBus
                .syncPublisher(COURSE_TOPIC)
                .onCourseUpdated(state.course)
        }
    }

    interface NewsUpdaterListener {
        @RequiresEdt
        fun onNewsUpdated(newsTree: NewsTree)
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

        fun user(project: Project): User? {
            return getInstance(project).state.user
        }
    }
}
