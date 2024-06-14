package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.ide.wizard.comment.CommentNewProjectWizardStep
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.MacOtherAction
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl
import com.intellij.openapi.components.service
import com.intellij.openapi.module.GeneralModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.UIBundle
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.util.minimumWidth
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.api.CourseConfig
import fi.aalto.cs.apluscourses.presentation.CourseItemViewModel
import fi.aalto.cs.apluscourses.services.CoursesClient
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import icons.PluginIcons
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import org.yaml.snakeyaml.Yaml
import javax.swing.Icon
import javax.swing.JEditorPane
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionListener

class APlusModuleBuilder : GeneratorNewProjectWizardBuilderAdapter(APlusModuleBuilderA())

/**
 * [com.intellij.ide.wizard.language.EmptyProjectGeneratorNewProjectWizard]
 */
class APlusModuleBuilderA : GeneratorNewProjectWizard {
    override val icon: Icon = PluginIcons.A_PLUS_LOGO_COLOR
    override val name: String = MyBundle.message("intellij.ProjectBuilder.name")
    override val id: String = "APLUS_MODULE_TYPE"
    override val ordinal: Int = 100000
    override val description: String = MyBundle.message("intellij.ProjectBuilder.description")
    override fun isEnabled(): Boolean {
        val lastAction = (ActionManager.getInstance() as ActionManagerImpl).lastPreformedActionId
        return if (lastAction == null) true else !lastAction.contains("Module")
    }

    override fun createStep(context: WizardContext): NewProjectWizardChainStep<Step> =
        RootNewProjectWizardStep(context)
            .nextStep(::CommentStep)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::CourseSelectStep)
            .nextStep(::Step)

    private class CommentStep(parent: NewProjectWizardStep) : CommentNewProjectWizardStep(parent) {
        override val comment: String =
            "A project integrating with the A+ LMS, supporting course module downloads<br>and assignment submissions."
        //UIBundle.message("label.project.wizard.empty.project.generator.full.description")
    }

    class Step(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
        override fun setupProject(project: Project) {
            val moduleType = ModuleTypeManager.getInstance().findByID(GeneralModuleType.TYPE_ID)
            val builder = moduleType.createModuleBuilder()
            setupProjectFromBuilder(project, builder)
        }
    }

    private class CourseSelectStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
        var urlField: JBTextField? = null
        var courses: List<CourseItemViewModel> = emptyList()
        val configs: MutableMap<CourseItemViewModel, CourseConfig.JSON> = mutableMapOf()
        val courseList = JBList<CourseItemViewModel>()
        var selectedCourse: CourseItemViewModel? = null
        var languages = listOf("Finnish", "English")
        var languageCombo: ComboBox<String>? = null
        var languageComment: JEditorPane? = null
        var com = "test"
        val test = ListSelectionListener { e ->
            if (e.valueIsAdjusting) return@ListSelectionListener
            selectedCourse = courseList.selectedValue
            urlField?.text = selectedCourse?.url
            val config = configs[selectedCourse]
            println(config)
            if (config == null || languageCombo == null) return@ListSelectionListener
            println("${config.id} ${languageCombo!!.selectedItem}")
            languages = config.languages.map { language -> languageCodeToName(language) }
            if (languages.size > 1 && languages.contains("Finnish")) {
                com = MyBundle.message("ui.courseProject.view.languagePrompt")
                languageComment?.text = MyBundle.message("ui.courseProject.view.languagePrompt")
            } else {
                com = ""
                languageComment?.text = ""
            }
            languageCombo!!.removeAllItems()
            languages.forEach { language -> languageCombo!!.addItem(language) }
            println(languages.joinToString(", "))
        }

        private val NAME_TEXT_ATTRIBUTES
                : SimpleTextAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, null)
        private val SEMESTER_TEXT_ATTRIBUTES
                : SimpleTextAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null)

        init {
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
            courseList.selectionMode = ListSelectionModel.SINGLE_SELECTION

            courseList.addListSelectionListener(test)

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

                for (course in courses) {
                    val courseConfig = client.getBody<CourseConfig.JSON>(course.url, false)
                    println("${course.name} ${courseConfig.languages.joinToString(", ")}")
                    configs[course] = courseConfig
                }
            }
        }

        override fun setupUI(builder: Panel) {
            with(builder) {
                row {
                    label(MyBundle.message("ui.courseProject.courseSelection.selection"))
                }
                row {
                    cell(courseList).align(Align.FILL)
                }
                row(MyBundle.message("ui.courseProject.courseSelection.textField")) {
                    textField().resizableColumn().align(Align.FILL).apply {
                        urlField = component
                    }
                    button("Fetch") { event -> println("press") }
                }
                row("Language:") {
                    comboBox(languages).comment("", 40).apply {
                        languageCombo = component
                        languageComment = comment
                    }
                }
                row("Settings:") {
                    checkBox("Leave IntelliJ settings unchanged")
                        .comment(MyBundle.message("ui.courseProject.form.settingsWarningText"), 40)
                }
            }
        }

    }

}
