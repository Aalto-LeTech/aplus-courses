package fi.aalto.cs.apluscourses.model

import com.intellij.notification.Notification
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier
import fi.aalto.cs.apluscourses.intellij.notifications.NewModulesVersionsNotification
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier
import fi.aalto.cs.apluscourses.model.CourseUpdater.CourseConfigurationFetcher
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.utils.CoursesClient
import fi.aalto.cs.apluscourses.utils.Event
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle
import fi.aalto.cs.apluscourses.utils.Version
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

class CourseUpdater
/**
 * Construct a course updater with the given parameters.
 */(
    private val courseProject: CourseProject,
    private val course: Course,
    private val project: Project,
    private val courseUrl: URL,
    private val configurationFetcher: CourseConfigurationFetcher,
    private val eventToTrigger: Event,
    private val notifier: Notifier,
    updateInterval: Long
) {
    fun interface CourseConfigurationFetcher {
        @Throws(IOException::class)
        fun fetch(configurationUrl: URL?): InputStream
    }

    private val notifiedModules: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var moduleUpdatesNotification: Notification? = null

    /**
     * Construct a course updater with reasonable defaults.
     */
    constructor(
        courseProject: CourseProject,
        course: Course,
        project: Project,
        courseUrl: URL,
        eventToTrigger: Event
    ) : this(
        courseProject, course, project, courseUrl, CourseConfigurationFetcher { url: URL? ->
            CoursesClient.fetch(
                url!!
            )
        }, eventToTrigger, DefaultNotifier(),
        PluginSettings.UPDATE_INTERVAL
    )

    fun doTask() {

        val progressViewModel =
            PluginSettings.getInstance().getMainViewModel(project).progressViewModel
        val progress =
            progressViewModel.start(3, PluginResourceBundle.getText("ui.ProgressBarView.refreshingCourse"), false)
        val selectedLanguage = PluginSettings
            .getInstance()
            .getCourseFileManager(project).language

        val auth = courseProject.authentication
        try {
            if (auth != null) {
                val news = course.exerciseDataSource.getNews(course, auth, selectedLanguage)
                val newsTree = NewsTree(news)
                courseProject.newsTree = newsTree
                eventToTrigger.trigger()
            }
        } catch (e: IOException) {
            progress.finish()
            return
        }
        progress.increment()
        val newCourseConfig: JSONObject?
        try {
            newCourseConfig = fetchCourseConfiguration()
        } catch (e: JSONException) {
            progress.finish()
            return
        } catch (e: IOException) {
            progress.finish()
            return
        }
        progress.increment()
        updateModules(fetchModulesInfo(newCourseConfig))
        course.exerciseDataSource.updateCacheExpiration(newCourseConfig.optLong("courseLastModified"))
        if (Thread.interrupted()) {
            progress.finish()
            return
        }
        notifyUpdatableModules()
        progress.finish()
        eventToTrigger.trigger()
    }

    @Throws(IOException::class)
    private fun fetchCourseConfiguration(): JSONObject {
        val inputStream = configurationFetcher.fetch(courseUrl)
        val tokenizer = JSONTokener(inputStream)
        return JSONObject(tokenizer)
    }

    private fun fetchModulesInfo(courseConfiguration: JSONObject): Map<URI?, ModuleInfo> {
        try {
            val array = courseConfiguration.getJSONArray("modules")
            // The equals and hashCode methods of the URL class can cause DNS lookups, so URI instances
            // are preferred in maps.
            val mapping: MutableMap<URI?, ModuleInfo> = HashMap()
            for (i in 0 until array.length()) {
                val module = array.getJSONObject(i)
                val url = URL(module.getString("url"))
                val moduleInfo = ModuleInfo(
                    Version.fromString(module.optString("version", "1.0")),
                    module.optString("changelog", "")
                )
                mapping[urlToUri(url)] = moduleInfo
            }
            return mapping
        } catch (e: IOException) {
            return emptyMap()
        } catch (e: JSONException) {
            return emptyMap()
        }
    }

    private fun updateModules(uriToModuleInfo: Map<URI?, ModuleInfo>) {
        for (module in course.modules) {
            val moduleInfo = uriToModuleInfo[urlToUri(module.url)]
            if (moduleInfo != null) {
                module.updateVersion(moduleInfo.version)
                module.updateChangelog(moduleInfo.changelog)
            }
        }
    }

    private fun notifyUpdatableModules() {
        val updatableModules = course.updatableModules
            .stream()
            .filter { m: Module -> !m.hasLocalChanges() || m.isMajorUpdate }
            .filter { m: Module -> notifiedModules.add(m.name) }
            .collect(Collectors.toList())
        if (updatableModules.isNotEmpty()) {
            moduleUpdatesNotification?.expire()

            val notification = NewModulesVersionsNotification(updatableModules)
            moduleUpdatesNotification = notification
            notifier.notifyAndHide(notification, project)
        }
    }

    private class ModuleInfo(
        val version: Version,
        val changelog: String
    )

    companion object {
        private fun urlToUri(url: URL): URI? {
            return try {
                url.toURI()
            } catch (e: URISyntaxException) {
                null
            }
        }
    }
}
