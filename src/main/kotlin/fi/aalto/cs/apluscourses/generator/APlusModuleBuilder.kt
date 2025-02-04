package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.util.application
import fi.aalto.cs.apluscourses.services.ProjectInitializationTracker
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import org.jetbrains.annotations.NonNls

internal class APlusModuleBuilder : ModuleBuilder() {

    override fun getModuleType(): ModuleType<*> = APlusModuleType()
    override fun canCreateModule(): Boolean = false
    override fun getWeight(): Int = 100000
    override fun isAvailable(): Boolean {
        val lastAction = (ActionManager.getInstance() as ActionManagerImpl).lastPreformedActionId
        @NonNls val moduleAction = "Module"
        return lastAction == null || !lastAction.contains(moduleAction)
    }

    private val config: APlusModuleConfig = APlusModuleConfig()

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

        val module = application.runWriteAction<Module> {
            ProjectRootManager.getInstance(project).projectSdk = config.jdk
            super.createAndCommitIfNeeded(project, model, true)
        }

        project.service<ProjectInitializationTracker>()
            .addInitializationTask {
                val startTime = System.currentTimeMillis()

                // The version string only starts with "java version" when the JDK is not downloaded completely
                while ((ProjectRootManager.getInstance(
                        project
                    ).projectSdk?.versionString?.startsWith("java version")
                        ?: true) && System.currentTimeMillis() - startTime < 300 * 1000
                ) {
                    Thread.sleep(1000)
                }
            }


        return listOf(module)
    }

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep =
        CourseSelectStep(config)

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
}

