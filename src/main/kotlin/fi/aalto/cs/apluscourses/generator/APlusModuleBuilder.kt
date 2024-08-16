package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.JavaUiBundle
import com.intellij.ide.projectWizard.ProjectWizardJdkComboBox
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.roots.ui.configuration.JdkComboBox
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Placeholder
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.Plugins
import fi.aalto.cs.apluscourses.services.course.CoursesFetcher
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionListener

//internal class APlusModuleBuilder : GeneratorNewProjectWizardBuilderAdapter(APlusModuleBuilderA()) {
//    override fun canCreateModule(): Boolean = false
//}

internal class APlusModuleBuilder : ModuleBuilder() {
    override fun getModuleType(): ModuleType<*> = APlusModuleType()
    override fun canCreateModule(): Boolean = false
    override fun getWeight(): Int = 100000
    override fun isAvailable(): Boolean {
        val lastAction = (ActionManager.getInstance() as ActionManagerImpl).lastPreformedActionId
        return if (lastAction == null) true else !lastAction.contains("Module")
    }

    private var courseConfig: CourseConfig.JSON? = null
    private fun updateCourseConfig(courseConfig: CourseConfig.JSON) {
//        courseSettingsStep.update(courseConfig)
        this.courseConfig = courseConfig
    }

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep =
        CourseSelectStep()

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> = arrayOf(CourseSettingsStep(wizardContext, modulesProvider))

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
                .fetchCourses({ courses -> courseList.setListData(courses.toTypedArray()) }, { courseList.updateUI() })
        }

        override fun getComponent(): JComponent {
            return panel {
                panel {
                    row {
                        text("A project linked to the A+ LMS, supporting course module downloads and assignment submissions.").applyToComponent {
                            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                        }
                    }
                    row {
                        text("Select a course:")
                    }
                    row {
                        cell(courseList).align(Align.FILL)
                    }
                    row("Configuration file URL:") {
                        textField()
                            .bindText(courseConfigUrl)
                            .resizableColumn()
                            .align(Align.FILL)
                    }
                    row {
                        text("").bindText(errorMessage).applyToComponent {
                            foreground = JBUI.CurrentTheme.NotificationError.borderColor()
                        }
                    }
                }.customize(UnscaledGaps(32, 32, 32, 32))
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
            this@APlusModuleBuilder.updateCourseConfig(courseConfig)
            return true
        }
    }

    inner class CourseSettingsStep(
        val wizardContext: WizardContext,
        val modulesProvider: ModulesProvider
    ) : ModuleWizardStep() {
        private var mainPanel = panel {}
        var placeholder: Placeholder? = null

        override fun updateStep() {
            val courseConfig = this@APlusModuleBuilder.courseConfig ?: return
            mainPanel = panel {
                panel {
                    group("Language") {
                        row {
                            text(
                                MyBundle.message("ui.courseProject.view.languagePrompt"), 120
                            )
                        }
                        row {
                            segmentedButton(courseConfig.languages) {
                                text = languageCodeToName(it)
                            }//.bind(selectedLanguage)
                        }
                    }
                    group("Settings") {
                        row {
                            text(
                                MyBundle.message("ui.courseProject.form.settingsWarningText"), 120
                            )
                        }
                        row {
                            checkBox("Leave IntelliJ settings unchanged")
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
                    group("Additional configuration") {
                        row(JavaUiBundle.message("label.project.wizard.new.project.jdk")) {
                            cell(ProjectWizardJdkComboBox(null, wizardContext.disposable))
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
            // do nothing
        }

        override fun validate(): Boolean {
            return true
        }
    }
}

