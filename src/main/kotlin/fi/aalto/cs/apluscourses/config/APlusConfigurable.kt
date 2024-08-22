package fi.aalto.cs.apluscourses.config

import com.intellij.ide.plugins.MultiPanel
import com.intellij.ide.plugins.newui.TabbedPaneHeaderComponent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.ui.TokenForm
import fi.aalto.cs.apluscourses.ui.overview.ResponsiveImagePanel
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil.languageCodeToName
import fi.aalto.cs.apluscourses.utils.PluginVersion
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel

internal class APlusConfigurable(val project: Project) : Configurable, Configurable.TopComponentProvider,
    Configurable.NoScroll {

    private val originalLanguage = AtomicProperty<String>("")
    private val selectedLanguage = AtomicProperty<String>("")

    private var isLanguageChanged = false

    private val settingsTab = 0
    private val aboutTab = 1

    private var myTabHeaderComponent: TabbedPaneHeaderComponent? = null
    override fun getCenterComponent(controller: Configurable.TopComponentController): Component =
        myTabHeaderComponent ?: JPanel()

    private var settingsPanel: DialogPanel? = null
    private val tokenForm = TokenForm(project)

    private var banner: ResponsiveImagePanel? = null
    override fun createComponent(): JComponent {
        val course = CourseManager.course(project)
        val currentLanguage = CourseFileManager.getInstance(project).state.language ?: ""
        selectedLanguage.set(currentLanguage)
        originalLanguage.set(currentLanguage)
        selectedLanguage.afterChange { isLanguageChanged = originalLanguage.get() != it }
        val languages = course?.languages ?: emptyList()
        banner = ResponsiveImagePanel(icon = CoursesIcons.About.Banner, width = 200)
        settingsPanel = panel {
            group("Global Settings") {
                with(tokenForm) {
                    user()
                    token()
                    validation()
                }
//                row("Assistant mode") {
//                    checkBox("Enable assistant mode").applyToComponent {
//                        isEnabled = false
//                    }
//                } TODO: Re-enable when assistant mode is implemented
            }
            group("Course Settings") {
                row("Language") {
                    segmentedButton(languages) {
                        text = languageCodeToName(it)
                    }.bind(selectedLanguage)
                }
            }
        }
        val myCardPanel: MultiPanel = object : MultiPanel() {
            override fun create(key: Int): JComponent {
                if (key == settingsTab) {
                    return settingsPanel!!
                }
                if (key == aboutTab) {
                    return aboutTab()
                }
                return super.create(key)
            }

            private var oldWidth = 0
            override fun getWidth(): Int {
                val newWidth = super.getWidth()

                if (newWidth != oldWidth && this.isShowing) {
                    oldWidth = newWidth
                    banner?.updateWidth(newWidth)
                }
                return newWidth
            }
        }
        myTabHeaderComponent = TabbedPaneHeaderComponent(DefaultActionGroup()) { index ->
            myCardPanel.select(index, true)
        }
        myTabHeaderComponent?.addTab("Settings", null)
        myTabHeaderComponent?.addTab("About", null)
        myCardPanel.select(settingsTab, true)
        myTabHeaderComponent?.setListener()
        myTabHeaderComponent?.getComponent(1)?.isVisible = false
        return myCardPanel
    }

    private val pluginPage = "https://plugins.jetbrains.com/plugin/13634-a-courses"
    private val aPlus = "https://plus.cs.aalto.fi/"
    private val github = "https://github.com/Aalto-LeTech/aplus-courses"

    private fun aboutTab() = panel {
        row {
            cell(banner!!)
        }
        row {
            text(MyBundle.message("ui.aboutDialog.title")).applyToComponent {
                font = JBFont.h0()
            }.comment(PluginVersion.current)
        }
        row {
            text(
                MyBundle.message("ui.aboutDialog.description")
            )
        }
        row {
            browserLink(
                MyBundle.message("ui.aboutDialog.website"), pluginPage
            )
        }
        row {
            browserLink(
                MyBundle.message("ui.aboutDialog.GithubWebsite"), github
            )
        }
        row {
            browserLink(
                MyBundle.message("ui.aboutDialog.APlusWebsite"), aPlus
            )
        }
        row {
            text(
                MyBundle.message("ui.aboutDialog.authors")
            )
        }
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
        return "A+ Courses"
    }
}
