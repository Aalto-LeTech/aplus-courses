package fi.aalto.cs.apluscourses.activities

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.api.CourseConfig.resourceUrls
import fi.aalto.cs.apluscourses.notifications.CourseVersionOutdatedError
import fi.aalto.cs.apluscourses.notifications.CourseVersionTooNewError
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.SdkInstall
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.SettingsImporter
import fi.aalto.cs.apluscourses.utils.*
import fi.aalto.cs.apluscourses.utils.Version.ComparisonStatus
import fi.aalto.cs.apluscourses.utils.temp.PluginAutoInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


internal class InitializationActivity() :
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

        val pluginVersion = PluginVersion.currentVersion
        val requiredVersion = courseConfig.version
        logger.info("Starting initialization for course ${courseConfig.name} with required plugin version $requiredVersion and installed version $pluginVersion")

        val versionComparison = requiredVersion.comparisonStatus(pluginVersion)

        when (versionComparison) {
            ComparisonStatus.MAJOR_TOO_OLD -> {
                logger.warn("A+ Courses version outdated: installed $pluginVersion, required $requiredVersion")
                Notifier.notify(CourseVersionOutdatedError(), project)
                return
            }

            ComparisonStatus.MAJOR_TOO_NEW -> {
                logger.warn("A+ Courses version too new: installed $pluginVersion, required $requiredVersion")
                Notifier.notify(CourseVersionTooNewError(), project)
            }

            else -> {}
        }

        ProjectViewUtil.ignoreFileInProjectView(PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME, project)

        val needsRestartForPlugins =
            PluginAutoInstaller.ensureDependenciesInstalled(
                project,
                courseConfig.requiredPlugins,
                askForConsent = true
            ) == false

        val courseFileManager = CourseFileManager.getInstance(project)
        val isCourseInitialized = courseFileManager.state.initialized

        var needsRestartForSettings = false

        if (!isCourseInitialized) {
            courseFileManager.state.initialized = true
            val settingsImporter = SettingsImporter.getInstance(project)
            settingsImporter.importProjectSettings(resourceUrls(courseConfig.resources))

            if (courseFileManager.state.importSettings) {
                settingsImporter.importVMOptions(courseConfig.vmOptions)
                settingsImporter.importIdeSettings(resourceUrls(courseConfig.resources))
                needsRestartForSettings = true
            }
        }

        if ((needsRestartForPlugins || needsRestartForSettings)) {
            project.service<SdkInstall>().waitForInstall()
            val willRestart = withContext(Dispatchers.EDT) {
                askForIDERestart()
            }
            if (willRestart) {
                application.invokeLater {
                    (application as ApplicationEx).restart(true)
                }
            }
        }

        project.service<CourseManager>().restart()

//        if (!PluginIntegrityChecker.isPluginCorrectlyInstalled()) {
//            logger.warn("Missing one or more dependencies")
//            ApplicationManager.getApplication().invokeLater { IntegrityCheckDialog.show() }
//        }

        withContext(Dispatchers.EDT) {
            ToolWindowManager.getInstance(project).getToolWindow("A+ Courses")?.activate(null)
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
