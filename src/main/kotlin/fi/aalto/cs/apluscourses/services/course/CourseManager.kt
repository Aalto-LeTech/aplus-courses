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
    class State {
        var authenticated: Boolean? = null
        var course: Course? = null
        var courseName: String? = null
        var news: NewsList? = null
        var user: User? = null
        var feedbackCss: String? = null
        var aPlusUrl: String? = null
        var grading: CourseConfig.Grading? = null
        var settingsImported = false
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

    val state = State()

    private val notifiedModules: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var job: Job? = null
    fun restart() {
        job?.cancel(CancellationException("test"))
        run()
    }

    fun stop() {
        job?.cancel(CancellationException("test"))
    }

    private fun run(
        updateInterval: Long = PluginSettings.UPDATE_INTERVAL
    ) {
        job =
            cs.launch {
                try {
                    while (true) {
                        withBackgroundProgress(project, "A+ Courses") {
                            reportSequentialProgress { reporter ->
                                reporter.indeterminateStep("Refreshing course")
                                doTask()
                            }
                        }
                        cs.ensureActive()
                        delay(updateInterval)
                    }
                } catch (_: CancellationException) {
                }
            }
    }

    fun setNewsAsRead() {
        val news = state.news ?: return
        news.setAllRead()
        fireNewsUpdated(news)
    }

    private suspend fun doTask() {
        project.service<CourseFileManager>().migrateOldConfig()
        println(CourseFileManager.getInstance(project).state.url)
        val courseConfig = CourseConfig.get(project) ?: return

        state.courseName = courseConfig.name
        state.aPlusUrl = courseConfig.aPlusUrl
        println("courseConfig: $courseConfig")
        if (!TokenStorage.getInstance().isTokenSet()) {
            state.clearAll()
            state.authenticated = false
            fireCourseUpdated()
            return
        }
        state.authenticated = true

        try {
            runBlocking {
                state.grading = courseConfig.grading
//                async {
                val extraCourseData = APlusApi.Course(courseConfig.id.toLong()).get(project)
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
                state.course?.components?.values?.forEach { it.load() }
//                }
//                async {
                state.user = withContext(Dispatchers.IO) {
                    APlusApi.me().get(project)
                }
//                }
            }
            val course = state.course ?: return
            course.autoInstallComponents.forEach {
                val status = it.loadAndGetStatus()
                if (status == Component.Status.UNRESOLVED && it is Module) {
                    installModule(it, false)
                }
            }
            fireCourseUpdated()
            ExercisesUpdater.getInstance(project).restart()
            refreshModuleStatuses()
            val newNews = APlusApi.Course(course.id).news(project)
            state.news?.news?.forEach {
                if (it.isRead) newNews.setRead(it.id)
            }

            state.news = newNews
            fireNewsUpdated(newNews)
        } catch (e: IOException) {
            return
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
            withBackgroundProgress(project, "A+ Courses") {
                reportSequentialProgress { reporter ->
                    reporter.indeterminateStep("Installing ${module.name}")
                    module.downloadAndInstall()
                    state.course?.callbacks?.invokePostDownloadModuleCallbacks(project, module)
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

    fun updateModule(module: Module) {
        cs.launch {
            module.update()
            refreshModuleStatuses()
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


    private fun fireNewsUpdated(newsList: NewsList) {
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

        fun user(project: Project): User? {
            return getInstance(project).state.user
        }

        fun authenticated(project: Project): Boolean? {
            return getInstance(project).state.authenticated
        }
    }
}
