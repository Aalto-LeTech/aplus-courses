package fi.aalto.cs.apluscourses.activities

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import fi.aalto.cs.apluscourses.intellij.model.SettingsImporter
import fi.aalto.cs.apluscourses.intellij.notifications.*
import fi.aalto.cs.apluscourses.intellij.utils.ProjectKey
import fi.aalto.cs.apluscourses.intellij.utils.ProjectViewUtil
import fi.aalto.cs.apluscourses.model.*
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.course.CourseUpdaterService
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.ui.IntegrityCheckDialog
import fi.aalto.cs.apluscourses.ui.utils.PluginInstallerDialogs
import fi.aalto.cs.apluscourses.utils.*
import fi.aalto.cs.apluscourses.utils.Version.ComparisonStatus
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty
import org.json.JSONException
import java.io.IOException
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

internal class InitializationActivity(private val notifier: Notifier = DefaultNotifier()) :
    ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<ExercisesUpdaterService>().restart()
        project.service<CourseUpdaterService>().restart()
        val courseVersion = BuildInfo.INSTANCE.courseVersion
        logger.info("Starting initialization, course version $courseVersion")

        if (!PluginIntegrityChecker.isPluginCorrectlyInstalled()) {
            logger.warn("Missing one or more dependencies")
            ApplicationManager.getApplication().invokeLater { IntegrityCheckDialog.show() }
        }

        val pluginSettings = PluginSettings.getInstance()
        pluginSettings.initializeLocalIdeSettings()

        ProjectViewUtil.ignoreFileInProjectView(PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME, project)

        val mainVm = PluginSettings.getInstance().getMainViewModel(project)
        val progressViewModel = mainVm.progressViewModel
        val cardVm = mainVm.toolWindowCardViewModel

        val courseConfigurationFileUrl = getCourseUrlFromProject(project)
        if (courseConfigurationFileUrl == null) {
            isInitialized(project).set(true)
            progressViewModel.stopAll()
            logger.info("No course configuration found")
            return
        }

        val course: Course
        try {
            course = Course.fromUrl(
                courseConfigurationFileUrl,
                IntelliJModelFactory(project), project
            )
            println("course")
            println(course.getModules().joinToString { "\"${it.name}\"" })
        } catch (e: UnexpectedResponseException) {
            logger.warn("Error occurred while trying to parse a course configuration file", e)
            notifier.notify(CourseConfigurationError(e), project)
            isInitialized(project).set(true)
            progressViewModel.stopAll()
            return
        } catch (e: MalformedCourseConfigurationException) {
            logger.warn("Error occurred while trying to parse a course configuration file", e)
            notifier.notify(CourseConfigurationError(e), project)
            isInitialized(project).set(true)
            progressViewModel.stopAll()
            return
        } catch (e: IOException) {
            logger.info("IOException occurred while using the HTTP client", e)
            notifier.notify(NetworkErrorNotification(e), project)
            isInitialized(project).set(true)
            progressViewModel.stopAll()

//            cardVm.isNetworkError = true

            return
        }

        cardVm.setModuleButtonRequiresLogin(course.requiresLoginForModules())

        val progress = progressViewModel.start(3, PluginResourceBundle.getText("ui.ProgressBarView.loading"), false)
        progress.increment()

        importSettings(project, course)
        progress.increment()

        ApplicationManager.getApplication().invokeLater {
            if (java.lang.Boolean.FALSE == PluginAutoInstaller.ensureDependenciesInstalled(
                    project, notifier,
                    course.requiredPlugins
                ) { pluginNames: List<PluginDependency?>? ->
                    PluginInstallerDialogs.askForInstallationConsentOnInit(
                        pluginNames!!
                    )
                } && PluginInstallerDialogs.askForIDERestart()
            ) {
                (ApplicationManager.getApplication() as ApplicationEx).restart(true)
            }
        }

        val versionComparison = courseVersion.compareTo(course.version)

        if (versionComparison == ComparisonStatus.MAJOR_TOO_OLD
            || versionComparison == ComparisonStatus.MAJOR_TOO_NEW
        ) {
            if (versionComparison == ComparisonStatus.MAJOR_TOO_OLD) {
                logger.warn("A+ Courses version outdated: installed $courseVersion, required ${course.version}")
            } else {
                logger.warn("A+ Courses version too new: installed $courseVersion, required ${course.version}")
            }
            notifier.notify(
                if (versionComparison == ComparisonStatus.MAJOR_TOO_OLD
                ) CourseVersionOutdatedError() else CourseVersionTooNewError(), project
            )
            progress.finish()
            return
        } else if (versionComparison == ComparisonStatus.MINOR_TOO_OLD) {
            logger.warn("A+ Courses version outdated: installed $courseVersion, required ${course.version}")
            notifier.notify(CourseVersionOutdatedWarning(), project)
        }
        println(course.getModules().joinToString("\n"))

        val courseProject = CourseProject(
            course, courseConfigurationFileUrl, project,
            DefaultNotifier()
        )
        PluginSettings.getInstance().registerCourseProject(courseProject)
        isInitialized(project).set(true)
        progress.finish()
        logger.info("Initialization done")
        return
    }

    private fun importSettings(project: Project, course: Course) {
        val basePath = project.basePath
        if (basePath != null) {
            try {
                val settingsImporter = SettingsImporter()
                settingsImporter.importCustomProperties(Paths.get(project.basePath!!), course, project)
                settingsImporter.importFeedbackCss(project, course)
            } catch (e: IOException) {
                logger.warn("Failed to import settings", e)
                notifier.notify(NetworkErrorNotification(e), project)
            }
        }
    }

}

private val logger: Logger = APlusLogger.logger
private val initializedProjects
        : MutableMap<ProjectKey, ObservableProperty<Boolean>> = ConcurrentHashMap()
private val projectListener: ProjectManagerListener = object : ProjectManagerListener {
    override fun projectClosed(project: Project) {
        initializedProjects.remove(ProjectKey(project))
        ProjectManager.getInstance().removeProjectManagerListener(project, this)
    }
}

/**
 * Returns the observable boolean corresponding to whether the initialization activity for the
 * given project has completed or not.
 */
fun isInitialized(project: Project): ObservableProperty<Boolean> {
    return initializedProjects.computeIfAbsent(ProjectKey(project)) {
        ProjectManager.getInstance().addProjectManagerListener(project, projectListener)
        ObservableReadWriteProperty(false)
    }
}

private fun getCourseUrlFromProject(project: Project): URL? {
    if (project.isDefault) {
        return null
    }

    try {
        val isCourseProject = PluginSettings.getInstance()
            .getCourseFileManager(project)
            .load()
        return if (isCourseProject) {
            PluginSettings.getInstance().getCourseFileManager(project).courseUrl
        } else {
            null
        }
    } catch (e: IOException) {
        logger.warn("Malformed project course tag file", e)
        return null
    } catch (e: JSONException) {
        logger.warn("Malformed project course tag file", e)
        return null
    }
}
