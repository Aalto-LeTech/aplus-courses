package fi.aalto.cs.apluscourses.activities

import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.ToolWindowManager
import fi.aalto.cs.apluscourses.model.*
import fi.aalto.cs.apluscourses.notifications.*
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.SettingsImporter
import fi.aalto.cs.apluscourses.utils.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


internal class InitializationActivity(private val notifier: Notifier.Companion = Notifier) :
    ProjectActivity {
    override suspend fun execute(project: Project) {
//        project.service<ExercisesUpdaterService>().restart()
//        project.service<CourseManager>().restart()
        withContext(Dispatchers.EDT) {
            ToolWindowManager.getInstance(project).getToolWindow("A+ Courses")?.activate(null)
        }
        val connection = project.messageBus.connect()
        connection.subscribe(ModuleListener.TOPIC, object : ModuleListener {
            override fun moduleRemoved(project: Project, module: Module) {
                CourseManager.getInstance(project).refreshModuleStatuses()
            }
        })
        val pluginVersion = BuildInfo.pluginVersion
        logger.info("Starting initialization, course version $pluginVersion")

//        if (!PluginIntegrityChecker.isPluginCorrectlyInstalled()) {
//            logger.warn("Missing one or more dependencies")
//            ApplicationManager.getApplication().invokeLater { IntegrityCheckDialog.show() }
//        }

//        val pluginSettings = PluginSettings.getInstance()
//        pluginSettings.initializeLocalIdeSettings()

        ProjectViewUtil.ignoreFileInProjectView(PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME, project)

//        val mainVm = PluginSettings.getInstance().getMainViewModel(project)
//        val progressViewModel = mainVm.progressViewModel
//        val cardVm = mainVm.toolWindowCardViewModel

//        val courseConfigurationFileUrl = getCourseUrlFromProject(project)
//        if (courseConfigurationFileUrl == null) {
//            isInitialized(project).set(true)
//            progressViewModel.stopAll()
//            logger.info("No course configuration found")
//            return
//        }

//        val course: Course
//        try {
//            course = Course.fromUrl(
//                courseConfigurationFileUrl,
//                IntelliJModelFactory(project), project
//            )
//            println("course")
//            println(course.oldModules.joinToString { "\"${it.originalName}\"" })
//        } catch (e: UnexpectedResponseException) {
//            logger.warn("Error occurred while trying to parse a course configuration file", e)
//            notifier.notify(CourseConfigurationError(e), project)
//            isInitialized(project).set(true)
//            progressViewModel.stopAll()
//            return
//        } catch (e: MalformedCourseConfigurationException) {
//            logger.warn("Error occurred while trying to parse a course configuration file", e)
//            notifier.notify(CourseConfigurationError(e), project)
//            isInitialized(project).set(true)
//            progressViewModel.stopAll()
//            return
//        } catch (e: IOException) {
//            logger.info("IOException occurred while using the HTTP client", e)
//            notifier.notify(NetworkErrorNotification(e), project)
//            isInitialized(project).set(true)
//            progressViewModel.stopAll()
//
////            cardVm.isNetworkError = true
//
//            return
//        }

//        cardVm.setModuleButtonRequiresLogin(course.requireAuthenticationForModules)
//
//        val progress = progressViewModel.start(3, PluginResourceBundle.getText("ui.ProgressBarView.loading"), false)
//        progress.increment()
//
//        importSettings(project, course)
//        progress.increment()
//
//        ApplicationManager.getApplication().invokeLater {
//            if (java.lang.Boolean.FALSE == PluginAutoInstaller.ensureDependenciesInstalled(
//                    project,
//                    course.requiredPlugins
//                ) { pluginNames: List<PluginDependency?>? ->
//                    PluginInstallerDialogs.askForInstallationConsentOnInit(
//                        pluginNames!!
//                    )
//                } && PluginInstallerDialogs.askForIDERestart()
//            ) {
//                (ApplicationManager.getApplication() as ApplicationEx).restart(true)
//            }
//        }
//
//        val versionComparison = courseVersion.comparisonStatus(course.courseVersion)
//
//        if (versionComparison == ComparisonStatus.MAJOR_TOO_OLD
//            || versionComparison == ComparisonStatus.MAJOR_TOO_NEW
//        ) {
//            if (versionComparison == ComparisonStatus.MAJOR_TOO_OLD) {
//                logger.warn("A+ Courses version outdated: installed $courseVersion, required ${course.courseVersion}")
//            } else {
//                logger.warn("A+ Courses version too new: installed $courseVersion, required ${course.courseVersion}")
//            }
//            notifier.notify(
//                if (versionComparison == ComparisonStatus.MAJOR_TOO_OLD
//                ) CourseVersionOutdatedError() else CourseVersionTooNewError(), project
//            )
//            progress.finish()
//            return
//        } else if (versionComparison == ComparisonStatus.MINOR_TOO_OLD) {
//            logger.warn("A+ Courses version outdated: installed $courseVersion, required ${course.courseVersion}")
//            notifier.notify(CourseVersionOutdatedWarning(), project)
//        }
//        println(course.oldModules.joinToString("\n"))
//
//        val courseProject = CourseProject(
//            course, courseConfigurationFileUrl, project
//        )
//        PluginSettings.getInstance().registerCourseProject(courseProject)
//        isInitialized(project).set(true)
//        progress.finish()
//        logger.info("Initialization done")
//        return
    }

    private fun importSettings(project: Project, course: Course) { // TODO
        val basePath = project.basePath
        if (basePath != null) {
            try {
//                val settingsImporter = SettingsImporter()
//                settingsImporter.importCustomProperties(Paths.get(project.basePath!!), course, project)
//                settingsImporter.importFeedbackCss(project, course)
            } catch (e: IOException) {
                logger.warn("Failed to import settings", e)
                notifier.notify(NetworkErrorNotification(e), project)
            }
        }
    }

}

private val logger: Logger = APlusLogger.logger
//private val initializedProjects
//        : MutableMap<ProjectKey, ObservableProperty<Boolean>> = ConcurrentHashMap()
//private val projectListener: ProjectManagerListener = object : ProjectManagerListener {
//    override fun projectClosed(project: Project) {
//        initializedProjects.remove(ProjectKey(project))
//        ProjectManager.getInstance().removeProjectManagerListener(project, this)
//    }
//}

/**
 * Returns the observable boolean corresponding to whether the initialization activity for the
 * given project has completed or not.
 */
//fun isInitialized(project: Project): ObservableProperty<Boolean> {
//    return initializedProjects.computeIfAbsent(ProjectKey(project)) {
//        ProjectManager.getInstance().addProjectManagerListener(project, projectListener)
//        ObservableReadWriteProperty(false)
//    }
//}

private fun getCourseUrlFromProject(project: Project): Url? {
    if (project.isDefault) {
        return null
    }
    return null

//    try {
//        val isCourseProject = PluginSettings.getInstance()
//            .getCourseFileManager(project)
//            .load()
//        return if (isCourseProject) {
//            PluginSettings.getInstance().getCourseFileManager(project).courseUrl
//        } else {
//            null
//        }
//    } catch (e: IOException) {
//        logger.warn("Malformed project course tag file", e)
//        return null
//    } catch (e: JSONException) {
//        logger.warn("Malformed project course tag file", e)
//        return null
//    }
}
