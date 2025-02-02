package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.course.CoursesFetcher
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionListener

class CourseSelectStep(val config: APlusModuleConfig) : ModuleWizardStep() {
    private val courseList = JBList<CoursesFetcher.CourseConfig>()
    private val courseConfigUrl = AtomicProperty("")
    private val errorMessage = AtomicProperty("")

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
        config.courseConfig = courseConfig
        config.courseConfigUrl = url
        config.programmingLanguage = courseList.selectedValue.language ?: ""
        return true
    }
}
