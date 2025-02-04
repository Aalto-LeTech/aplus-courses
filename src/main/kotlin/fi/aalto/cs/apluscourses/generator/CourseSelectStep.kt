package fi.aalto.cs.apluscourses.generator

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.and
import com.intellij.openapi.observable.util.isNull
import com.intellij.openapi.observable.util.notEqualsTo
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.course.CoursesFetcher
import fi.aalto.cs.apluscourses.ui.Utils.bindItem
import fi.aalto.cs.apluscourses.ui.Utils.list
import javax.swing.JComponent
import javax.swing.JList

class CourseSelectStep(private val config: APlusModuleConfig) : ModuleWizardStep() {
    private val courses = AtomicProperty<List<CoursesFetcher.CourseConfig>>(emptyList())

    private val courseConfigUrl = AtomicProperty("")
    private val course = AtomicProperty<CoursesFetcher.CourseConfig?>(null)
    private val language = AtomicProperty(PluginSettings.SUPPORTED_LANGUAGES.first())

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

        application.service<CoursesFetcher>()
            .fetchCourses { courses.set(it) }
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
                    list(courses, object : ColoredListCellRenderer<CoursesFetcher.CourseConfig>() {
                        override fun customizeCellRenderer(
                            list: JList<out CoursesFetcher.CourseConfig>,
                            item: CoursesFetcher.CourseConfig?,
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
                    }).resizableColumn().align(AlignX.FILL)
                        .bindItem(course)
                        .validationOnApply {
                            if (course.get() == null) {
                                return@validationOnApply ValidationInfo(message("ui.ExportModuleDialog.error.noModule"))
                            }
                            return@validationOnApply null
                        }
                }
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
            }.customize(UnscaledGaps(20, 20, 20, 20))
        }
    }

    override fun updateDataModel() {}

    override fun validate(): Boolean {
        val url = courseConfigUrl.get()
        if (url.isEmpty()) {
            throw ConfigurationException(
                message("generator.APlusModuleBuilder.selectCourse.selectError"),
                message("generator.APlusModuleBuilder.selectCourse.selectErrorTitle")
            )
        }

        val courseConfig = application.service<CoursesFetcher>().fetchCourse(url)
            ?: throw ConfigurationException(
                message("generator.APlusModuleBuilder.selectCourse.urlError"),
                message("generator.APlusModuleBuilder.selectCourse.urlErrorTitle")
            )

        config.courseConfig = courseConfig
        config.courseConfigUrl = url
        config.programmingLanguage = language.get()
        return true
    }
}
