package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.ide.wizard.comment.CommentNewProjectWizardStep
import com.intellij.openapi.components.service
import com.intellij.openapi.module.GeneralModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.UIBundle
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Panel
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.presentation.CourseItemViewModel
import fi.aalto.cs.apluscourses.services.CoursesClient
import icons.PluginIcons
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import org.yaml.snakeyaml.Yaml
import javax.swing.Icon
import javax.swing.JList
import javax.swing.JPanel

class APlusModuleBuilder : GeneratorNewProjectWizardBuilderAdapter(APlusModuleBuilderA())

class APlusModuleBuilderA : GeneratorNewProjectWizard {
    override val icon: Icon = PluginIcons.A_PLUS_LOGO_COLOR
    override val name: String = MyBundle.message("intellij.ProjectBuilder.name")
    override val id: String = "APLUS_MODULE_TYPE"
    override val ordinal: Int = 100000
    override val description: String = MyBundle.message("intellij.ProjectBuilder.description")
//    override fun getModuleType(): APlusModuleType = APlusModuleType()
//
//    override fun getWeight(): Int = 100000
//
//    /**
//     * Only show the builder when creating a new project
//     */
//    override fun isAvailable(): Boolean {
//        val lastAction = (ActionManager.getInstance() as ActionManagerImpl).lastPreformedActionId
//        return if (lastAction == null) true else !lastAction.contains("Module")
//    }
//
//
//    override fun canCreateModule(): Boolean = false
//
//    override fun isOpenProjectSettingsAfter(): Boolean = false

    override fun createStep(context: WizardContext): NewProjectWizardChainStep<Step> =
        RootNewProjectWizardStep(context)
            .nextStep(::CommentStep)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::CourseSelectStep)
            .nextStep(::Step)

    private class CommentStep(parent: NewProjectWizardStep) : CommentNewProjectWizardStep(parent) {
        override val comment: String = UIBundle.message("label.project.wizard.empty.project.generator.full.description")
    }

    class Step(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
        override fun setupProject(project: Project) {
            val moduleType = ModuleTypeManager.getInstance().findByID(GeneralModuleType.TYPE_ID)
            val builder = moduleType.createModuleBuilder()
            setupProjectFromBuilder(project, builder)
        }
    }

    private class CourseSelectStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {

//            val projectSettings: ProjectSettingsStep = ProjectSettingsStep(context)

        val panel = JPanel()
        var courses: List<CourseItemViewModel> = emptyList()

        //            val urlFieldLabel = JBLabel(MyBundle.message("ui.courseProject.courseSelection.textField"))
        val courseListLabel = JBLabel(MyBundle.message("ui.courseProject.courseSelection.selection"))
        val urlField = JBTextField()
        val courseList = JBList<CourseItemViewModel>()

        private val NAME_TEXT_ATTRIBUTES
                : SimpleTextAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, null)
        private val SEMESTER_TEXT_ATTRIBUTES
                : SimpleTextAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null)

        init {

//                projectSettings.updateStep()
//                val layout = BoxLayout(panel, BoxLayout.Y_AXIS)
//                panel.setLayout(layout)
//                val border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
//                panel.border = border
//                projectSettings.addSettingsField(
//                    MyBundle.message("ui.courseProject.courseSelection.textField"),
//                    urlField
//                )
//                projectSettings.addSettingsComponent(courseList)
//                panel.add(courseListLabel)
//                urlField.setEmptyState(MyBundle.message())
//                panel.add(urlField)
            panel.add(courseList)
//                panel.add(Box.createVerticalGlue())
            courseList.setCellRenderer(
                object : ColoredListCellRenderer<CourseItemViewModel>() {
                    override fun customizeCellRenderer(
                        list: JList<out CourseItemViewModel>,
                        item: CourseItemViewModel,
                        index: Int,
                        selected: Boolean,
                        hasFocus: Boolean
                    ) {
                        icon = PluginIcons.A_PLUS_EXERCISE_GROUP
                        append(item.name, NAME_TEXT_ATTRIBUTES)
                        append(" " + item.semester, SEMESTER_TEXT_ATTRIBUTES)
                    }
                }
            )

            val client = application.service<CoursesClient>()
            val url = "https://version.aalto.fi/gitlab/aplus-courses/course-config-urls/-/raw/main/courses.yaml"

            client.cs.launch {
                val res = client.get(url)

                courses = Yaml()
                    .load<List<Map<String, String>>>(res.bodyAsText())
                    .map { course: Map<String, String> ->
                        CourseItemViewModel.fromMap(course)
                    }

                courseList.setListData(courses.toTypedArray())
            }
        }

        override fun setupUI(builder: Panel) {
            with(builder) {
                row {
                    label(MyBundle.message("ui.courseProject.courseSelection.selection"))
                }
                row {
                    cell(panel).resizableColumn()
                }
                row(MyBundle.message("ui.courseProject.courseSelection.textField")) {
                    textField().resizableColumn()
                }
            }

//            override fun getComponent(): JComponent = projectSettings.component

//            override fun updateStep() {
//
//            }

//            override fun updateDataModel() {
//                // don't do anything
//            }
        }
    }

}
