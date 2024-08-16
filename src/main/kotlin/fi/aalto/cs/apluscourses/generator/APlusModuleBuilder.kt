package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.ide.wizard.comment.CommentNewProjectWizardStep
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CoursesFetcher
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import javax.swing.Icon
import javax.swing.JEditorPane
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.event.ListSelectionListener

internal class APlusModuleBuilder : GeneratorNewProjectWizardBuilderAdapter(APlusModuleBuilderA()) {
    override fun canCreateModule(): Boolean = false
}

/**
 * [com.intellij.ide.wizard.language.EmptyProjectGeneratorNewProjectWizard]
 */
class APlusModuleBuilderA : GeneratorNewProjectWizard {
    override val icon: Icon = CoursesIcons.LogoColor
    override val name: String = MyBundle.message("intellij.ProjectBuilder.name")
    override val id: String = APlusModuleType.ID
    override val ordinal: Int = 100000
    override val description: String = MyBundle.message("intellij.ProjectBuilder.description")
    override fun isEnabled(): Boolean {
        val lastAction = (ActionManager.getInstance() as ActionManagerImpl).lastPreformedActionId
        return if (lastAction == null) true else !lastAction.contains("Module")
    }

    override fun createStep(context: WizardContext): NewProjectWizardChainStep<Step> {
//        val javaModuleType = JavaModuleType()
//        val test = JavaModuleType.getModuleType()
//            .createWizardSteps(context, javaModuleType.createModuleBuilder(), context.modulesProvider)
        return RootNewProjectWizardStep(context)
            .nextStep(::CommentStep)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::CourseSelectStep)
            .nextStep { Step(it) }
            .nextStep(::Step)
    }

    private class CommentStep(parent: NewProjectWizardStep) : CommentNewProjectWizardStep(parent) {
        override val comment: String =
            "A project integrating with the A+ LMS, supporting course module downloads<br>and assignment submissions."
        //UIBundle.message("label.project.wizard.empty.project.generator.full.description")
    }


    inner class Step(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
        override fun setupProject(project: Project) {
//            val moduleType = APlusModuleType.instance
//            val builder = moduleType.createModuleBuilder()
//            setupProjectFromBuilder(project, builder)
        }
    }

    private class CourseSelectStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {

//        private val javaStep = JavaModuleType.getModuleType().modifySettingsStep()

        val fetchEnabled = AtomicBooleanProperty(false)
        val languagesVisible = AtomicBooleanProperty(false)
        val settingsVisible = AtomicBooleanProperty(false)
        var urlField: JBTextField? = null
        val courseList = JBList<CoursesFetcher.CourseConfig>()
        var selectedCourse: CoursesFetcher.CourseConfig? = null

        data class Language(val code: String, val displayName: String) {
            override fun toString(): String = displayName
        }

        var languages = emptyList<Language>()
        var languageCombo: ComboBox<Language>? = null
        var languageComment: JEditorPane? = null
        var com = "test"
        override fun setupProject(project: Project) {
            val url = urlField?.text
            val languageIndex = languageCombo?.selectedIndex
            if (url == null || languageIndex == null) return
            val language = languages[languageIndex].code
            println("setupProject $url $language")
            project.service<CourseFileManager>().updateSettings(language, url)
        }

        val test = ListSelectionListener { e ->
            val course = courseList.selectedValue
            val config = course?.config
            if (config == null) {
                courseList.setSelectedValue(selectedCourse, false)
                return@ListSelectionListener
            }
            if (e.valueIsAdjusting) return@ListSelectionListener
            urlField!!.text = course.url
            selectCourse(course)
        }

        private val NAME_TEXT_ATTRIBUTES
                : SimpleTextAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, null)
        private val SEMESTER_TEXT_ATTRIBUTES
                : SimpleTextAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null)

        init {
            courseList.cellRenderer = object : ColoredListCellRenderer<CoursesFetcher.CourseConfig>() {
                override fun customizeCellRenderer(
                    list: JList<out CoursesFetcher.CourseConfig>,
                    item: CoursesFetcher.CourseConfig,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean
                ) {
                    val loading = item.config == null
                    isEnabled = !loading
                    isFocusable
                    icon = if (loading) CoursesIcons.Loading else CoursesIcons.ExerciseGroup
                    append(item.name, NAME_TEXT_ATTRIBUTES)
                    append(" " + item.semester, SEMESTER_TEXT_ATTRIBUTES)
                }
            }
            courseList.putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
            courseList.selectionMode = ListSelectionModel.SINGLE_SELECTION

            courseList.addListSelectionListener(test)

            application.service<CoursesFetcher>()
                .fetchCourses({ courses -> courseList.setListData(courses.toTypedArray()) }, { courseList.updateUI() })
        }

        fun selectCourse(course: CoursesFetcher.CourseConfig?) {
            selectedCourse = course
            val config = course?.config
            if (config == null) {
                languagesVisible.set(false)
                settingsVisible.set(false)
                return
            }
            languages = config.languages.map { code -> Language(code, languageCodeToName(code)) }
            if (languages.size > 1 && languages.contains(Language("fi", "Finnish"))) {
                com = MyBundle.message("ui.courseProject.view.languagePrompt")
                languageComment?.text = MyBundle.message("ui.courseProject.view.languagePrompt")
            } else {
                com = ""
                languageComment?.text = ""
            }
            languageCombo!!.removeAllItems()
            languages.forEach { language -> languageCombo!!.addItem(language) }
            languagesVisible.set(true)
            settingsVisible.set(config.resources != null)
            println(languages.joinToString(", "))
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
                        component.document.addDocumentListener(object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                fetchEnabled.set(component.text != selectedCourse?.url)
                            }
                        })
                    }
                    button("Fetch") { event ->
                        urlField?.let {
                            fetchEnabled.set(false)
                            application.service<CoursesFetcher>().fetchCourse(it.text) {
                                if (it == null) fetchEnabled.set(true)
                                courseList.setSelectedValue(null, false)
                                selectCourse(it)
                            }
                        }
                    }.apply {
                        enabledIf(fetchEnabled)
                    }
                }
                row("Language:") {
                    comboBox(languages).comment("", 40).apply {
                        languageCombo = component
                        languageComment = comment
                    }
                }.visibleIf(languagesVisible)
                row("Settings:") {
                    checkBox("Leave IntelliJ settings unchanged")
                }.visibleIf(settingsVisible)
                row("") {
                    comment(
                        MyBundle.message("ui.courseProject.form.settingsWarningText"), 40
                    )
                }.visibleIf(settingsVisible)
            }
        }

    }

}
