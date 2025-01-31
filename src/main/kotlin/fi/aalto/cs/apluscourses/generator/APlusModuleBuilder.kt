package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.starters.local.StarterContext
import com.intellij.ide.starters.shared.*
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
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.util.application
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import org.jetbrains.annotations.NonNls
import CourseSelectStep

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
        return listOf(module)
    }

    val projectTypes = listOf(StarterProjectType("1", "Maven"), StarterProjectType("2", "Gradle"))
    val languages = listOf(StarterLanguage("3", "Kotlin", "1"))
    val isExampleCodeProvided = false
    val isPackageNameEditable = true
    val languageLevels = listOf(StarterLanguageLevel("1", "17", "3"))
    val defaultLanguageLevel = null
    val packagingTypes = listOf(StarterAppPackaging("22", "JAR"))
    val applicationTypes = listOf(StarterAppType("d", "Web"))
    val testFrameworks = listOf(StarterTestRunner("l", "JUnit"))
    val customizedMessages = null
    val showProjectTypes = false

    val startSettings = StarterWizardSettings(
        projectTypes = projectTypes,
        languages = languages,
        isExampleCodeProvided = isExampleCodeProvided,
        isPackageNameEditable = isPackageNameEditable,
        languageLevels = languageLevels,
        defaultLanguageLevel = defaultLanguageLevel,
        packagingTypes = packagingTypes,
        applicationTypes = applicationTypes,
        testFrameworks = testFrameworks,
        customizedMessages = customizedMessages,
        showProjectTypes = showProjectTypes
    )

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep =
        CourseSelectStep(config)

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider,
    ): Array<ModuleWizardStep> = arrayOf(
        CourseSettingsStep(
            wizardContext,
            StarterContext(),
            this@APlusModuleBuilder,
            wizardContext.disposable,
            startSettings,
            config
        )
    )
}

