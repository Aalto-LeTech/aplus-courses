package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.services.ProjectInitializationTracker
import fi.aalto.cs.apluscourses.services.course.CourseSetupStatus
import fi.aalto.cs.apluscourses.services.course.SetupStepState
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownloadTracker
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VfsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.file.Path
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.annotations.NonNls
import kotlin.coroutines.resume
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.minutes

@NonNls
internal const val SCALA_LANGUAGE = "scala"

internal class APlusModuleBuilder : ModuleBuilder() {

    override fun getModuleType(): ModuleType<*> = APlusModuleType()
    override fun canCreateModule(): Boolean = false
    override fun getWeight(): Int = 100000
    override fun isAvailable(): Boolean {
        val lastAction = ActionManagerEx.getInstanceEx().lastPreformedActionId
        @NonNls val moduleAction = "Module"
        return lastAction == null || !lastAction.contains(moduleAction)
    }

    internal val config: APlusModuleConfig = APlusModuleConfig()

    override fun commit(
        project: Project,
        model: ModifiableModuleModel?,
        modulesProvider: ModulesProvider?
    ): List<Module> {
        CoursesLogger.info("Creating project from ${config.courseConfigUrl}, language: ${config.language}")
        project.service<CourseFileManager>().updateSettings(
            config.language,
            config.courseConfigUrl,
            config.importSettings
        )

        val jdk = config.jdk
        val module = application.runWriteAction<Module> {
            ProjectRootManager.getInstance(project).projectSdk = jdk
            super.createAndCommitIfNeeded(project, model, true)
        }

        if (jdk != null) {
            SdkDownloadTracker.getInstance().startSdkDownloadIfNeeded(jdk)
        }

        if (config.programmingLanguage == SCALA_LANGUAGE) {
            project.service<ProjectInitializationTracker>()
                .addInitializationTask { awaitJdkDownload(project) }
        }

        return listOf(module)
    }

    private suspend fun awaitJdkDownload(project: Project) {
        val sdk = readAction { ProjectRootManager.getInstance(project).projectSdk } ?: return
        if (isDownloading(sdk)) {
            val setup = CourseSetupStatus.getInstance(project)
            setup.report(CourseSetupStatus.JDK, message("ui.setup.jdk"), SetupStepState.RUNNING)

            val downloaded = withBackgroundProgress(project, message("services.progress.downloadingJdk")) {
                withTimeoutOrNull(JDK_DOWNLOAD_TIMEOUT) { awaitDownloadFinished(sdk) }
            }
            if (downloaded == null) {
                CoursesLogger.warn("Gave up waiting for the JDK download after $JDK_DOWNLOAD_TIMEOUT")
            }

            setup.update(CourseSetupStatus.JDK, SetupStepState.DONE)
        }

        if (isJdkReady(sdk)) {
            ensureSdkConfigured(sdk)
        } else {
            CoursesLogger.warn("The JDK download did not finish successfully")
        }
    }

    private suspend fun isJdkReady(sdk: Sdk): Boolean {
        if (isDownloading(sdk)) return false
        val home = sdk.homePath ?: return false
        return withContext(Dispatchers.IO) { Path.of(home).exists() }
    }

    private suspend fun awaitDownloadFinished(sdk: Sdk): Boolean = withContext(Dispatchers.EDT) {
        val listenerDisposable = Disposer.newDisposable(DOWNLOAD_LISTENER_NAME)
        try {
            suspendCancellableCoroutine { continuation ->
                val registered = SdkDownloadTracker.getInstance().tryRegisterDownloadingListener(
                    sdk,
                    listenerDisposable,
                    EmptyProgressIndicator(),
                ) { succeeded ->
                    if (continuation.isActive) continuation.resume(succeeded == true)
                }
                if (!registered && continuation.isActive) continuation.resume(true)
            }
        } finally {
            Disposer.dispose(listenerDisposable)
        }
    }

    private suspend fun isDownloading(sdk: Sdk): Boolean = withContext(Dispatchers.EDT) {
        SdkDownloadTracker.getInstance().isDownloading(sdk)
    }

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep =
        CourseSelectStep(config, parentDisposable)

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider,
    ): Array<ModuleWizardStep> = arrayOf(
        CourseSettingsStep(
            wizardContext,
            this@APlusModuleBuilder,
            wizardContext.disposable,
            config
        )
    )

    companion object {
        private val JDK_DOWNLOAD_TIMEOUT = 5.minutes

        @NonNls
        private const val DOWNLOAD_LISTENER_NAME = "A+ Courses JDK download listener"

        internal suspend fun ensureSdkConfigured(sdk: Sdk) {
            val home = sdk.homePath ?: return
            val hasRoots = readAction { sdk.rootProvider.getFiles(OrderRootType.CLASSES).isNotEmpty() }
            if (hasRoots) return

            CoursesLogger.info("Setting up roots for downloaded JDK ${sdk.name}")
            withContext(Dispatchers.IO) {
                VfsUtil.markDirtyAndRefresh(false, true, true, Path.of(home))
            }
            edtWriteAction {
                val type = sdk.sdkType as SdkType
                type.getVersionString(sdk)?.let { version ->
                    sdk.sdkModificator.apply { versionString = version }.commitChanges()
                }
                type.setupSdkPaths(sdk)
            }
        }
    }
}
