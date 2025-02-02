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

    private val projectTypes = listOf(StarterProjectType("1", "IntelliJ"))
    private val languages = listOf(StarterLanguage("1", "Scala", "1"))
    private val isExampleCodeProvided = false
    private val isPackageNameEditable = true
    private val languageLevels = listOf(StarterLanguageLevel("1", "17", "3"))
    private val defaultLanguageLevel = null
    private val packagingTypes = listOf(StarterAppPackaging("22", "JAR"))
    private val applicationTypes = listOf(StarterAppType("d", "Web"))
    private val testFrameworks = listOf(StarterTestRunner("l", "JUnit"))
    private val customizedMessages = null
    private val showProjectTypes = false

    private val startSettings = StarterWizardSettings(
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

