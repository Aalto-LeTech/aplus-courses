package fi.aalto.cs.apluscourses.activities

import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.application
import fi.aalto.cs.aplus.courseconfig.ConfigVersion
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.api.CourseConfigLoader
import fi.aalto.cs.apluscourses.api.CourseConfigLoader.resourceUrls
import fi.aalto.cs.apluscourses.notifications.CourseVersionOutdatedError
import fi.aalto.cs.apluscourses.notifications.CourseVersionOutdatedWarning
import fi.aalto.cs.apluscourses.notifications.CourseVersionTooNewError
import fi.aalto.cs.apluscourses.notifications.InvalidCourseUrlNotification
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.services.Notifier
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.ProjectInitializationTracker
import fi.aalto.cs.apluscourses.services.course.*
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.apluscourses.utils.PluginAutoInstaller
import fi.aalto.cs.apluscourses.utils.PluginVersion
import fi.aalto.cs.apluscourses.utils.PluginVersion.ComparisonStatus
import fi.aalto.cs.apluscourses.utils.ProjectViewUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls


internal class InitializationActivity :
    ProjectActivity {
    override suspend fun execute(project: Project) {
        val courseConfig = try {
            CourseConfigLoader.get(project)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CoursesLogger.warn("Network error in InitializationActivity", e)
            InitializationStatus.setIsIoError(project)
            CourseManager.getInstance(project).fireNetworkError(e)
            return
        }
        if (courseConfig == null) {
            InitializationStatus.setIsNotCourse(project)
            CourseManager.getInstance(project).fireNetworkError()
            return
        }
        try {
            CoursesClient.getInstance(project).changeHost(requireNotNull(courseConfig.aPlusUrl))
        } catch (e: IllegalArgumentException) {
            CoursesLogger.warn("Course configuration has an unusable A+ URL: ${courseConfig.aPlusUrl}", e)
            Notifier.notify(InvalidCourseUrlNotification(requireNotNull(courseConfig.aPlusUrl)), project)
            InitializationStatus.setIsIoError(project)
            CourseManager.getInstance(project).fireNetworkError(e)
            return
        }

        val connection = project.messageBus.connect()
        connection.subscribe(ModuleListener.TOPIC, object : ModuleListener {
            override fun moduleRemoved(project: Project, module: Module) {
                val isDumb = DumbService.isDumb(project)
                val courseManager = CourseManager.getInstance(project)
                val course = courseManager.state.course
                @NonNls val lastAction = ActionManagerEx.getInstanceEx().lastPreformedActionId ?: ""

                // Sometimes the module gets deleted when it is being installed in dumb mode
                if (isDumb && course != null &&
                    !lastAction.contains("Delete") // Do not trigger when deleting a module
                ) {
                    val moduleModel = course.getComponentIfExists(module.name)
                    if (moduleModel is fi.aalto.cs.apluscourses.model.component.Module) {
                        DumbService.getInstance(project).runWhenSmart {
                            application.runWriteAction {
                                moduleModel.loadToProject()
                            }
                        }
                    }
                }
                CourseManager.getInstance(project).refreshModuleStatuses()
            }
        })

        val pluginVersion = PluginVersion.currentVersion
        val requiredVersion = ConfigVersion.parse(courseConfig.version)
        CoursesLogger.info("Starting initialization for course ${courseConfig.name} with required plugin version $requiredVersion and installed version $pluginVersion")

        val versionComparison = pluginVersion.comparisonStatus(requiredVersion)

        when (versionComparison) {
            ComparisonStatus.MAJOR_TOO_OLD -> {
                CoursesLogger.warn("A+ Courses major version outdated: installed $pluginVersion, required $requiredVersion")
                Notifier.notify(CourseVersionOutdatedError(), project)
                InitializationStatus.setIsIoError(project)
                CourseManager.getInstance(project).fireNetworkError()
                return
            }

            ComparisonStatus.MINOR_TOO_OLD -> {
                CoursesLogger.warn("A+ Courses minor version outdated: installed $pluginVersion, required $requiredVersion")
                Notifier.notify(CourseVersionOutdatedWarning(), project)
            }

            ComparisonStatus.MAJOR_TOO_NEW -> {
                CoursesLogger.warn("A+ Courses version too new: installed $pluginVersion, required $requiredVersion")
                Notifier.notify(CourseVersionTooNewError(), project)
            }

            else -> {}
        }

        ProjectViewUtil.ignoreFileInProjectView(PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME)

        val courseFileManager = CourseFileManager.getInstance(project)
        val isCourseInitialized = courseFileManager.state.initialized

        val setup = CourseSetupStatus.getInstance(project)
        setup.report(
            CourseSetupStatus.CONFIGURATION,
            MyBundle.message("ui.setup.configuration"),
            SetupStepState.DONE
        )
        if (courseConfig.requiredPlugins.isNotEmpty()) {
            setup.report(
                CourseSetupStatus.PLUGINS,
                MyBundle.message("ui.setup.plugins"),
                SetupStepState.RUNNING
            )
        }
        if (!isCourseInitialized) {
            setup.report(
                CourseSetupStatus.SETTINGS,
                MyBundle.message("ui.setup.settings"),
                SetupStepState.PENDING
            )
        }

        project.service<CourseManager>().restart()
        withContext(Dispatchers.EDT) {
            ToolWindowManager.getInstance(project).getToolWindow("A+ Courses")?.activate(null)
        }

        val needsRestartForPlugins =
            !PluginAutoInstaller.ensureDependenciesInstalled(
                project,
                courseConfig.requiredPlugins,
                askForConsent = isCourseInitialized
            )


        var needsRestartForSettings = false

        if (!isCourseInitialized) {
            setup.update(CourseSetupStatus.SETTINGS, SetupStepState.RUNNING)
            courseFileManager.state.initialized = true
            val settingsImporter = SettingsImporter.getInstance(project)
            settingsImporter.importProjectSettings(resourceUrls(courseConfig.resources))
            courseConfig.scalaRepl?.arguments?.let {
                settingsImporter.importScalaReplAdditionalArguments(it)
            }

            if (courseFileManager.state.importSettings) {
                settingsImporter.importVMOptions(courseConfig.vmOptions)
                settingsImporter.importIdeSettings(resourceUrls(courseConfig.resources))
                needsRestartForSettings = true
            }
            setup.update(CourseSetupStatus.SETTINGS, SetupStepState.DONE)
        }

        if ((needsRestartForPlugins || needsRestartForSettings)) {
            CourseManager.getInstance(project).awaitFirstRefresh()
            project.service<ProjectInitializationTracker>().waitForAllTasks()
            val willRestart = withContext(Dispatchers.EDT) {
                askForIDERestart(project, newProject = !isCourseInitialized)
            }
            if (willRestart) {
                application.invokeLater {
                    (application as ApplicationEx).restart(true)
                }
            }
        }

//        if (!PluginIntegrityChecker.isPluginCorrectlyInstalled()) {
//            logger.warn("Missing one or more dependencies")
//            ApplicationManager.getApplication().invokeLater { IntegrityCheckDialog.show() }
//        }
    }

    private fun askForIDERestart(project: Project, newProject: Boolean) = Messages.showOkCancelDialog(
        project,
        MyBundle.message(
            if (newProject) "ui.newProject.askForIDERestart.message" else "ui.pluginInstallationDialog.askForIDERestart.message"
        ),
        MyBundle.message("ui.pluginInstallationDialog.askForIDERestart.title"),
        MyBundle.message("ui.pluginInstallationDialog.askForIDERestart.okText"),
        MyBundle.message("ui.pluginInstallationDialog.askForIDERestart.cancelText"),
        Messages.getQuestionIcon()
    ) == Messages.OK
}
