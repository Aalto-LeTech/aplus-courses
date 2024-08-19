package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.JavaUiBundle
import com.intellij.ide.projectWizard.ProjectWizardJdkComboBox
import com.intellij.ide.projectWizard.ProjectWizardJdkIntent
import com.intellij.ide.projectWizard.ProjectWizardJdkIntent.DownloadJdk
import com.intellij.ide.projectWizard.ProjectWizardJdkIntent.ExistingJdk
import com.intellij.ide.projectWizard.generators.JdkDownloadService
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
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.impl.jdkDownloader.JdkDownloadTask
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Placeholder
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.Plugins
import fi.aalto.cs.apluscourses.services.SdkInstall
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CoursesFetcher
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionListener

internal class APlusModuleBuilder : ModuleBuilder() {
    override fun getModuleType(): ModuleType<*> = APlusModuleType()
    override fun canCreateModule(): Boolean = false
    override fun getWeight(): Int = 100000
    override fun isAvailable(): Boolean {
        val lastAction = (ActionManager.getInstance() as ActionManagerImpl).lastPreformedActionId
        return if (lastAction == null) true else !lastAction.contains("Module")
    }

    private var courseConfig: CourseConfig.JSON? = null
    private var courseConfigUrl = ""
    private var programmingLanguage = ""
    private var language = ""
    private var sdk: ProjectWizardJdkIntent? = null
    private var importSettings = false


    override fun commit(
        project: Project,
        model: ModifiableModuleModel?,
        modulesProvider: ModulesProvider?
    ): List<Module?>? {
        println("Creating module $courseConfig, $courseConfigUrl, $language, $sdk")
        project.service<CourseFileManager>().updateSettings(
            language,
            courseConfigUrl,
            importSettings
        )
        val selectedSdk = sdk
        if (selectedSdk != null) {
            if (selectedSdk is DownloadJdk) {
                val task = selectedSdk.task
                if (task is JdkDownloadTask) {
                    println("Downloading SDK")
                    val sdkDownloadedFuture =
                        project.service<JdkDownloadService>().scheduleDownloadJdkForNewProject(task)
                    project.service<SdkInstall>().setFuture(sdkDownloadedFuture)
                }
            } else if (selectedSdk is ExistingJdk) {
                application.runWriteAction {
                    println("Setting SDK to ${selectedSdk.jdk}")
                    ProjectRootManager.getInstance(project).projectSdk = selectedSdk.jdk
                }
            }
        }
        val module = super.commit(project, model, modulesProvider)
        return module
    }

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep =
        CourseSelectStep()

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> = arrayOf(CourseSettingsStep(wizardContext))

    inner class CourseSelectStep : ModuleWizardStep() {
        val courseList = JBList<CoursesFetcher.CourseConfig>()
        val courseConfigUrl = AtomicProperty("")
        val errorMessage = AtomicProperty("")

        init {
            courseList.cellRenderer = object : ColoredListCellRenderer<CoursesFetcher.CourseConfig>() {
                override fun customizeCellRenderer(
                    list: JList<out CoursesFetcher.CourseConfig>,
                    item: CoursesFetcher.CourseConfig,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean
                ) {
                    icon = CoursesIcons.ExerciseGroup
                    append(item.name, SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, null))
                    append(" " + item.semester, SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null))
                }
            }
            courseList.putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
            courseList.selectionMode = ListSelectionModel.SINGLE_SELECTION

            courseList.addListSelectionListener(ListSelectionListener { e ->
                val url = courseList.selectedValue?.url ?: return@ListSelectionListener
                courseConfigUrl.set(url)
            })

            application.service<CoursesFetcher>()
                .fetchCourses { courses -> courseList.setListData(courses.toTypedArray()) }
        }

        override fun getComponent(): JComponent {
            return panel {
                panel {
                    row {
                        text("A project linked to an A+ LMS course").applyToComponent {
                            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                        }
                    }
                    row {
                        text("Select a course:")
                    }
                    row {
                        cell(courseList).resizableColumn().align(AlignX.FILL)
                    }
                    row("Configuration file URL:") {
                        textField()
                            .bindText(courseConfigUrl)
                            .resizableColumn()
                            .align(AlignX.FILL)
                    }
                    row {
                        text("").bindText(errorMessage).applyToComponent {
                            foreground = JBUI.CurrentTheme.NotificationError.borderColor()
                        }
                    }
                }.customize(UnscaledGaps(20, 20, 20, 20))
            }
        }

        override fun updateDataModel() {}

        override fun validate(): Boolean {
            val url = courseConfigUrl.get()
            if (url.isEmpty()) {
                errorMessage.set("Please select a course.")
                return false
            }
            val courseConfig = application.service<CoursesFetcher>().fetchCourse(url)
            if (courseConfig == null) {
                errorMessage.set("Invalid URL.")
                return false
            }
            errorMessage.set("")
            this@APlusModuleBuilder.courseConfig = courseConfig
            this@APlusModuleBuilder.courseConfigUrl = url
            this@APlusModuleBuilder.programmingLanguage = courseList.selectedValue.language ?: ""
            return true
        }
    }

    inner class CourseSettingsStep(
        val wizardContext: WizardContext
    ) : ModuleWizardStep() {
        private var mainPanel = panel {}
        private val selectedLanguage = AtomicProperty<String>("")
        private val dontImportSettings = AtomicProperty(false)
        private var selectedSdk: AtomicProperty<ProjectWizardJdkIntent>? = null
        private var placeholder: Placeholder? = null

        override fun updateStep() {
            val courseConfig = this@APlusModuleBuilder.courseConfig ?: return
            val languages = courseConfig.languages
            if (languages.contains("fi")) selectedLanguage.set("fi") else selectedLanguage.set(languages.first())

            mainPanel = panel {
                panel {
                    group("Language") {
                        row {
                            text(
                                MyBundle.message("ui.courseProject.view.languagePrompt")
                            )
                        }.visible(languages.size > 1 && languages.contains("fi"))
                        row {
                            segmentedButton(courseConfig.languages) {
                                text = languageCodeToName(it)
                            }.bind(selectedLanguage)
                        }
                    }
                    group("Settings") {
                        row {
                            text(
                                MyBundle.message("ui.courseProject.form.settingsWarningText")
                            )
                        }
                        row {
                            checkBox("Leave IntelliJ settings unchanged").bindSelected(dontImportSettings)
                        }
                    }
                    group("Required plugins") {
                        row {
                            text("The following plugins required by the course will be installed.")
                        }
                        row {
                            placeholder().apply {
                                placeholder = this
                            }.resizableColumn()
                        }
                    }
                    if (this@APlusModuleBuilder.programmingLanguage == "scala") {
                        group("Additional configuration") {
                            row(JavaUiBundle.message("label.project.wizard.new.project.jdk")) {
                                cell(ProjectWizardJdkComboBox(null, wizardContext.disposable)).apply {
                                    val defaultValue = this.component.selectedItem
                                    selectedSdk = AtomicProperty(defaultValue as ProjectWizardJdkIntent)
                                    bindItem(selectedSdk!!)
                                }
                            }
                        }
                    }
                }.customize(UnscaledGaps(32, 32, 32, 32))
            }

            component.revalidate()
            component.repaint()
            application.service<Plugins>().runInBackground(courseConfig.requiredPlugins) {
                placeholder?.component = it
                component.revalidate()
                component.repaint()
            }
        }

        override fun getComponent(): JComponent = mainPanel

        override fun updateDataModel() {
            this@APlusModuleBuilder.language = selectedLanguage.get()
            this@APlusModuleBuilder.sdk = selectedSdk?.get()
            this@APlusModuleBuilder.importSettings = !dontImportSettings.get()
        }

        override fun validate(): Boolean {
            return true
        }
    }
}

