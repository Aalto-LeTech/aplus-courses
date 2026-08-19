package fi.aalto.cs.apluscourses.config

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.ui.TokenForm
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import fi.aalto.cs.apluscourses.utils.PluginVersion
import javax.swing.JComponent

internal class APlusConfigurable(val project: Project) : Configurable {

    private val originalLanguage = AtomicProperty("")
    private val selectedLanguage = AtomicProperty("")

    private var isLanguageChanged = false

    private val tokenForm = TokenForm(project)

    override fun createComponent(): JComponent {
        val course = CourseManager.course(project)
        val currentLanguage = CourseFileManager.getInstance(project).state.language ?: ""
        selectedLanguage.set(currentLanguage)
        originalLanguage.set(currentLanguage)
        selectedLanguage.afterChange { isLanguageChanged = originalLanguage.get() != it }
        val languages = course?.languages ?: emptyList()

        return panel {
            group(message("config.APlusConfigurable.globalSettings")) {
                with(tokenForm) {
                    user()
                    token()
                    validation()
                }
            }
            group(message("config.APlusConfigurable.courseSettings")) {
                if (course == null) {
                    row {
                        text(message("config.APlusConfigurable.noCourse"))
                    }
                } else {
                    row(message("config.APlusConfigurable.courseName")) {
                        label(course.name)
                    }
                    languageRow(languages)
                }
            }.enabled(course != null)
            about()
        }
    }

    /**
     * A course with a single language offers no choice to make, so it is shown as plain text
     * instead of as a segmented button with one segment.
     */
    private fun Panel.languageRow(languages: List<String>) {
        when (languages.size) {
            0 -> {}
            1 -> row(message("config.APlusConfigurable.language")) {
                label(languageCodeToName(languages.first()))
            }

            else -> row(message("config.APlusConfigurable.language")) {
                segmentedButton(languages) {
                    text = languageCodeToName(it)
                }.bind(selectedLanguage)
            }
        }
    }

    private val pluginPage = "https://plugins.jetbrains.com/plugin/13634-a-courses"
    private val aPlus = "https://plus.cs.aalto.fi/"
    private val github = "https://github.com/Aalto-LeTech/aplus-courses"

    private fun Panel.about() {
        collapsibleGroup(message("config.APlusConfigurable.aboutTab")) {
            row(message("config.APlusConfigurable.version")) {
                label(PluginVersion.current)
            }
            row {
                text(message("ui.aboutDialog.description"))
            }
            row {
                browserLink(message("ui.aboutDialog.website"), pluginPage)
            }
            row {
                browserLink(message("ui.aboutDialog.GithubWebsite"), github)
            }
            row {
                browserLink(message("ui.aboutDialog.APlusWebsite"), aPlus)
            }
            row {
                text(message("ui.aboutDialog.authors"))
            }
        }.expanded = false
    }

    override fun isModified(): Boolean = isLanguageChanged || tokenForm.isModified

    override fun apply() {
        val originalLanguage = originalLanguage.get()
        val language = selectedLanguage.get()
        if (language.isNotEmpty() && originalLanguage != language) {
            CourseFileManager.getInstance(project).state.language = language
            CourseManager.getInstance(project).state.clearAll()
            ExercisesUpdater.getInstance(project).state.clearAll()
        }
        CourseManager.getInstance(project).restart()
    }

    override fun reset() {
        selectedLanguage.set(originalLanguage.get())
    }

    override fun getDisplayName(): String {
        return message("aplusCourses")
    }
}
