package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.and
import com.intellij.openapi.observable.util.isNull
import com.intellij.openapi.observable.util.notEqualsTo
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.course.CoursesFetcher
import fi.aalto.cs.apluscourses.ui.Utils.bindItem
import fi.aalto.cs.apluscourses.ui.Utils.list
import fi.aalto.cs.apluscourses.ui.Utils.stretchingScrollPane
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JList

class CourseSelectStep(
    private val config: APlusModuleConfig,
    parentDisposable: Disposable
) : ModuleWizardStep() {
    private val courses = AtomicProperty<List<CoursesFetcher.CourseInfo>>(emptyList())

    private val courseConfigUrl = AtomicProperty("")
    private val course = AtomicProperty<CoursesFetcher.CourseInfo?>(null)
    private val language = AtomicProperty(PluginSettings.SUPPORTED_LANGUAGES.first())

    private val courseList: JBList<CoursesFetcher.CourseInfo> =
        list(courses, object : ColoredListCellRenderer<CoursesFetcher.CourseInfo>() {
            override fun customizeCellRenderer(
                list: JList<out CoursesFetcher.CourseInfo>,
                item: CoursesFetcher.CourseInfo?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                icon = CoursesIcons.ExerciseGroup
                append(item?.name ?: "", SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, null))
                append(
                    (" " + item?.semester),
                    SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null)
                )
            }
        }).bindItem(course).apply {
            // JList takes its preferred scrollable viewport height from visibleRowCount.
            visibleRowCount = MIN_VISIBLE_COURSES
        }

    private val listPanel = JBLoadingPanel(BorderLayout(), parentDisposable).apply {
        add(ScrollPaneFactory.createScrollPane(courseList, true), BorderLayout.CENTER)
        setLoadingText(message("generator.APlusModuleBuilder.selectCourse.loading"))
    }

    private val mainPanel: DialogPanel
    private val scrollPane: JBScrollPane

    init {
        course.afterChange {
            if (it != null) {
                courseConfigUrl.set(it.url)
                language.set(it.language ?: "")
            }
        }
        courseConfigUrl.afterChange {
            if (course.get()?.url != it) {
                course.set(null)
            }
        }

        mainPanel = createPanel()
        scrollPane = stretchingScrollPane(mainPanel)

        courseList.emptyText.text = ""
        listPanel.startLoading()
        application.service<CoursesFetcher>().fetchCourses { result ->
            listPanel.stopLoading()
            result
                .onSuccess {
                    courses.set(it)
                    courseList.emptyText.text = message("generator.APlusModuleBuilder.selectCourse.empty")
                }
                .onFailure {
                    courseList.emptyText.text = message("generator.APlusModuleBuilder.selectCourse.loadFailed")
                }
        }
    }

    private fun createPanel(): DialogPanel = panel {
        row {
            text(message("generator.APlusModuleBuilder.description")).applyToComponent {
                foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            }
        }
        row {
            text(message("generator.APlusModuleBuilder.selectCourse"))
        }
        row {
            cell(listPanel)
                .resizableColumn()
                .align(Align.FILL)
                .validationOnApply {
                    if (course.get() == null) {
                        return@validationOnApply ValidationInfo(message("ui.ExportModuleDialog.error.noModule"))
                    }
                    return@validationOnApply null
                }
        }.resizableRow()
        row(message("generator.APlusModuleBuilder.configUrl")) {
            textField()
                .bindText(courseConfigUrl)
                .resizableColumn()
                .align(AlignX.FILL)
        }
        row(message("generator.APlusModuleBuilder.language")) {
            comboBox(PluginSettings.SUPPORTED_LANGUAGES)
                .bindItem(language)
        }.visibleIf(
            course
                .isNull()
                .and(
                    courseConfigUrl
                        .notEqualsTo("")
                )
        )
    }.apply {
        border = JBUI.Borders.empty(20)
    }

    override fun getComponent(): JComponent = scrollPane

    override fun updateDataModel() {}

    override fun validate(): Boolean {
        val url = courseConfigUrl.get()
        if (url.isEmpty()) {
            throw ConfigurationException(
                message("generator.APlusModuleBuilder.selectCourse.selectError"),
                message("generator.APlusModuleBuilder.selectCourse.selectErrorTitle")
            )
        }

        val courseConfig = runWithModalProgressBlocking(
            ModalTaskOwner.component(mainPanel),
            message("generator.APlusModuleBuilder.selectCourse.loadingConfig")
        ) {
            application.service<CoursesFetcher>().fetchCourse(url)
        } ?: throw ConfigurationException(
            message("generator.APlusModuleBuilder.selectCourse.urlError"),
            message("generator.APlusModuleBuilder.selectCourse.urlErrorTitle")
        )

        config.courseConfig = courseConfig
        config.courseConfigUrl = url
        config.programmingLanguage = language.get()
        return true
    }

    private companion object {
        const val MIN_VISIBLE_COURSES = 5
    }
}
