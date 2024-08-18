package fi.aalto.cs.apluscourses.activities

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.impl.jdkDownloader.JdkDownloader
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownload
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.api.CourseConfig.resourceUrls
import fi.aalto.cs.apluscourses.model.*
import fi.aalto.cs.apluscourses.notifications.*
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.SdkInstall
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.SettingsImporter
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.utils.*
import fi.aalto.cs.apluscourses.utils.temp.PluginAutoInstaller
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


internal class InitializationActivity(private val notifier: Notifier.Companion = Notifier) :
    ProjectActivity {
    override suspend fun execute(project: Project) {
        val courseConfig = CourseConfig.get(project) ?: return
        CoursesClient.getInstance(project).changeHost(courseConfig.aPlusUrl)

        val connection = project.messageBus.connect()
        connection.subscribe(ModuleListener.TOPIC, object : ModuleListener {
            override fun moduleRemoved(project: Project, module: Module) {
                CourseManager.getInstance(project).refreshModuleStatuses()
            }
        })
        val pluginVersion = BuildInfo.pluginVersion
        logger.info("Starting initialization, course version $pluginVersion")
        ProjectViewUtil.ignoreFileInProjectView(PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME, project)

        val needsRestartForPlugins =
            PluginAutoInstaller.ensureDependenciesInstalled(project, courseConfig.requiredPlugins) == false

        val courseFileManager = CourseFileManager.getInstance(project)
        val isCourseInitialized = courseFileManager.state.initialized

        val needsRestartForSettings = if (!isCourseInitialized) {
            courseFileManager.state.initialized = true
            val settingsImporter = SettingsImporter.getInstance(project)
            settingsImporter.importProjectSettings(resourceUrls(courseConfig.resources))
            if (courseFileManager.state.importSettings) {
                settingsImporter.importVMOptions(courseConfig.vmOptions)
                settingsImporter.importIdeSettings(resourceUrls(courseConfig.resources))
            } else {
                false
            }
        } else {
            false
        }


        if ((needsRestartForPlugins || needsRestartForSettings)) {
            project.service<SdkInstall>().waitForInstall {
                val willRestart = withContext(Dispatchers.EDT) {
                    askForIDERestart()
                }
                if (willRestart) {
                    application.invokeLater {
                        (application as ApplicationEx).restart(true)
                    }
                }
            }
        }

//        project.service<ExercisesUpdaterService>().restart()
        project.service<CourseManager>().restart()

//        if (!PluginIntegrityChecker.isPluginCorrectlyInstalled()) {
//            logger.warn("Missing one or more dependencies")
//            ApplicationManager.getApplication().invokeLater { IntegrityCheckDialog.show() }
//        }

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
        withContext(Dispatchers.EDT) {
            ToolWindowManager.getInstance(project).getToolWindow("A+ Courses")?.activate(null)
        }
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

    private fun askForIDERestart() = Messages.showOkCancelDialog(
        MyBundle.message("ui.pluginInstallationDialog.askForIDERestart.message"),
        MyBundle.message("ui.pluginInstallationDialog.askForIDERestart.title"),
        MyBundle.message("ui.pluginInstallationDialog.askForIDERestart.okText"),
        MyBundle.message("ui.pluginInstallationDialog.askForIDERestart.cancelText"),
        Messages.getQuestionIcon()
    ) == Messages.OK


    private val logger: Logger = APlusLogger.logger
}
