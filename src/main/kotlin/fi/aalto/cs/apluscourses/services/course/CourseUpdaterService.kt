package fi.aalto.cs.apluscourses.services.course

import com.intellij.notification.Notification
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.api.Course
import fi.aalto.cs.apluscourses.api.Course.News.Body
import fi.aalto.cs.apluscourses.model.news.NewsTree
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.utils.Version
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class CourseUpdaterService(
    private val project: Project,
    val cs: CoroutineScope
) {//: SimplePersistentStateComponent<CourseUpdaterService.State>(State()) {
    //class State : BaseState()


    fun interface CourseConfigurationFetcher {
        @Throws(IOException::class)
        fun fetch(configurationUrl: URL?): InputStream
    }

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

    private suspend fun doTask() {

//        val progressViewModel =
//            PluginSettings.getInstance().getMainViewModel(project).progressViewModel
//        val progress =
//            progressViewModel.start(3, PluginResourceBundle.getText("ui.ProgressBarView.refreshingCourse"), false)
//        val selectedLanguage = PluginSettings
//            .getInstance()
//            .getCourseFileManager(project).language

//        val auth = courseProject.authentication
        try {
//            if (auth != null) {
//            val course = Course(342)
//            val newsApi = Course.News(course)//course.exerciseDataSource.getNews(course, auth, selectedLanguage)
//            val res = withContext(Dispatchers.IO) {
//                project.service<CoursesClient>().client.get(course.news())
//            }
//            println(res)
            project.service<CoursesClient>().updateAuthentication()
            val newsTree = runBlocking {
                Course(342).news().get(project)
            }
            fireNewsUpdated(newsTree)


//            val news: List<News> = res.map { }
//            val newsTree = NewsTree(res)

//            val newsTreeViewModel = NewsTreeViewModel(newsTree)
//                courseProject.newsTree = newsTree
//                eventToTrigger.trigger()
//            }
        } catch (e: IOException) {
//            progress.finish()
            return
        }
//        progress.increment()
//        val newCourseConfig: JSONObject?
//        try {
//            newCourseConfig = fetchCourseConfiguration()
//        } catch (e: JSONException) {
//            progress.finish()
//            return
//        } catch (e: IOException) {
//            progress.finish()
//            return
//        }
//        progress.increment()
//        updateModules(fetchModulesInfo(newCourseConfig))
//        course.exerciseDataSource.updateCacheExpiration(newCourseConfig.optLong("courseLastModified"))
//        if (Thread.interrupted()) {
//            progress.finish()
//            return
//        }
//        notifyUpdatableModules()
//        progress.finish()
//        eventToTrigger.trigger()
    }

//    @Throws(IOException::class)
//    private suspend fun fetchCourseConfiguration(): JSONObject {
//        val inputStream = project.service<CoursesClient>().fetch(courseUrl)
//        val tokenizer = JSONTokener(inputStream)
//        return JSONObject(tokenizer)
//    }
//
//    private fun fetchModulesInfo(courseConfiguration: JSONObject): Map<URI?, ModuleInfo> {
//        try {
//            val array = courseConfiguration.getJSONArray("modules")
//            // The equals and hashCode methods of the URL class can cause DNS lookups, so URI instances
//            // are preferred in maps.
//            val mapping: MutableMap<URI?, ModuleInfo> = HashMap()
//            for (i in 0 until array.length()) {
//                val module = array.getJSONObject(i)
//                val url = URL(module.getString("url"))
//                val moduleInfo = ModuleInfo(
//                    Version.fromString(module.optString("version", "1.0")),
//                    module.optString("changelog", "")
//                )
//                mapping[urlToUri(url)] = moduleInfo
//            }
//            return mapping
//        } catch (e: IOException) {
//            return emptyMap()
//        } catch (e: JSONException) {
//            return emptyMap()
//        }
//    }

//    private fun updateModules(uriToModuleInfo: Map<URI?, ModuleInfo>) {
//        for (module in course.getModules()) {
//            val moduleInfo = uriToModuleInfo[urlToUri(module.url)]
//            if (moduleInfo != null) {
//                module.updateVersion(moduleInfo.version)
//                module.updateChangelog(moduleInfo.changelog)
//            }
//        }
//    }

//    private fun notifyUpdatableModules() {
//        val updatableModules = course.updatableModules
//            .stream()
//            .filter { m: Module -> !m.hasLocalChanges() || m.isMajorUpdate }
//            .filter { m: Module -> notifiedModules.add(m.name) }
//            .collect(Collectors.toList())
//        if (updatableModules.isNotEmpty()) {
//            moduleUpdatesNotification?.expire()
//
//            val notification = NewModulesVersionsNotification(updatableModules)
//            moduleUpdatesNotification = notification
//            notifier.notifyAndHide(notification, project)
//        }
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
                .onNewsUpdated(newsTree)
        }
    }

    interface NewsUpdaterListener {
        @RequiresEdt
        fun onNewsUpdated(newsTree: NewsTree)
    }

    companion object {
        private fun urlToUri(url: URL): URI? {
            return try {
                url.toURI()
            } catch (e: URISyntaxException) {
                null
            }
        }

        @ProjectLevel
        val NEWS_TOPIC: Topic<NewsUpdaterListener> =
            Topic(NewsUpdaterListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        fun getInstance(project: Project): CourseUpdaterService {
            return project.service<CourseUpdaterService>()
        }
    }
}
