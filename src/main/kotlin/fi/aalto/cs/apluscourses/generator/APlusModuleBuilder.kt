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
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Placeholder
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.Plugins
import fi.aalto.cs.apluscourses.services.ProjectInitializationTracker
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CoursesFetcher
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import kotlinx.coroutines.future.asDeferred
import org.jetbrains.annotations.NonNls
import java.awt.event.ItemEvent
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
        @NonNls val moduleAction = "Module"
        return lastAction == null || !lastAction.contains(moduleAction)
    }

    private var courseConfig: CourseConfig.JSON? = null
    private var courseConfigUrl = ""
    private var programmingLanguage = ""
    private var language = ""
    private var jdkIntent: ProjectWizardJdkIntent? = null
    private var importSettings = false


    override fun commit(
        project: Project,
        model: ModifiableModuleModel?,
        modulesProvider: ModulesProvider?
    ): List<Module>? {
        CoursesLogger.info("Creating project from $courseConfigUrl, language: $language")
        project.service<CourseFileManager>().updateSettings(
            language,
            courseConfigUrl,
            importSettings
        )
        val selectedSdk = jdkIntent
        if (selectedSdk != null) {
            if (selectedSdk is DownloadJdk) {
                val task = selectedSdk.task
                if (task is JdkDownloadTask) {
                    CoursesLogger.info("Downloading JDK ${task.jdkItem} for new project")

                    val sdkDownloadedFuture =
                        project.service<JdkDownloadService>().scheduleDownloadJdkForNewProject(task)
                    project.service<ProjectInitializationTracker>()
                        .addInitializationTask(sdkDownloadedFuture.asDeferred())
                }
            } else if (selectedSdk is ExistingJdk) {
                application.runWriteAction {
                    CoursesLogger.info("Setting SDK to ${selectedSdk.jdk} for new project")
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
                        text(message("generator.APlusModuleBuilder.description")).applyToComponent {
                            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                        }
                    }
                    row {
                        text(message("generator.APlusModuleBuilder.selectCourse"))
                    }
                    row {
                        cell(courseList).resizableColumn().align(AlignX.FILL)
                    }
                    row(message("generator.APlusModuleBuilder.configUrl")) {
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
                errorMessage.set(message("generator.APlusModuleBuilder.selectCourse.selectError"))
                return false
            }
            val courseConfig = application.service<CoursesFetcher>().fetchCourse(url)
            if (courseConfig == null) {
                errorMessage.set(message("generator.APlusModuleBuilder.selectCourse.urlError"))
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
        private var mainPanel = JBScrollPane()
        private val selectedLanguage = AtomicProperty<String>("")
        private val dontImportSettings = AtomicProperty(false)
        private var selectedSdk: AtomicProperty<ProjectWizardJdkIntent>? = null
        private var placeholder: Placeholder? = null

        override fun updateStep() {
            val courseConfig = this@APlusModuleBuilder.courseConfig ?: return
            val languages = courseConfig.languages
            @NonNls val finnishCode = "fi"
            if (languages.contains(finnishCode)) selectedLanguage.set(finnishCode) else selectedLanguage.set(languages.first())

            mainPanel = JBScrollPane(panel {
                panel {
                    group(message("generator.APlusModuleBuilder.language")) {
                        row {
                            text(
                                message("generator.APlusModuleBuilder.finnishInfo")
                            )
                        }.visible(languages.size > 1 && languages.contains(finnishCode))
                        row {
                            segmentedButton(courseConfig.languages) {
                                text = languageCodeToName(it)
                            }.bind(selectedLanguage)
                        }
                    }
                    group(message("generator.APlusModuleBuilder.settings")) {
                        row {
                            text(
                                message("generator.APlusModuleBuilder.settingsInfo")
                            )
                        }
                        row {
                            checkBox(message("generator.APlusModuleBuilder.leaveSettings")).bindSelected(
                                dontImportSettings
                            )
                        }
                    }
                    if (this@APlusModuleBuilder.programmingLanguage == "scala") {
                        group(message("generator.APlusModuleBuilder.extra")) {
                            row(JavaUiBundle.message("label.project.wizard.new.project.jdk")) {
                                cell(ProjectWizardJdkComboBox(null, wizardContext.disposable)).apply {
                                    val defaultValue = this.component.selectedItem
                                    selectedSdk = AtomicProperty(defaultValue as ProjectWizardJdkIntent)
                                    bindItem(selectedSdk!!)

                                    // If the user selects a JDK that is already installed, select the existing JDK
                                    component.addItemListener { changed ->
                                        if (changed.stateChange == ItemEvent.SELECTED) {
                                            val newSdk = changed.item
                                            if (newSdk is DownloadJdk) {
                                                val existingSdk =
                                                    component.registered.find { it.jdk.name == newSdk.task.suggestedSdkName }
                                                if (existingSdk != null) {
                                                    selectedSdk!!.set(existingSdk)
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        selectedSdk = null
                    }
                    if (courseConfig.requiredPlugins.isNotEmpty()) {
                        group(message("generator.APlusModuleBuilder.plugins")) {
                            row {
                                text(message("generator.APlusModuleBuilder.pluginsInfo"))
                            }
                            row {
                                placeholder().apply {
                                    placeholder = this
                                }.resizableColumn().align(AlignX.FILL)
                            }
                        }
                    }
                }.customize(UnscaledGaps(32, 32, 32, 32))
            }).apply {
                verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            }

            component.revalidate()
            component.repaint()
            if (courseConfig.requiredPlugins.isEmpty()) return
            application.service<Plugins>().runInBackground(courseConfig.requiredPlugins) { components ->
                placeholder?.component = panel {
                    components.map {
                        it.remove(4) // Remove checkbox and install button
                        it.remove(3)
                        it
                    }.forEach {
                        row {
                            contextHelp(
                                it.pluginDescriptor.description
                                    ?: message("generator.APlusModuleBuilder.defaultDescription")
                            )
                            cell(it).resizableColumn().align(AlignX.FILL).applyToComponent {
                                background = null
                                isOpaque = false
                            }
                        }.topGap(TopGap.SMALL)
                    }
                }
                component.revalidate()
                component.repaint()
            }
        }

        override fun getComponent(): JComponent = mainPanel

        override fun updateDataModel() {
            this@APlusModuleBuilder.language = selectedLanguage.get()
            this@APlusModuleBuilder.jdkIntent = selectedSdk?.get()
            this@APlusModuleBuilder.importSettings = !dontImportSettings.get()
        }
    }
}

