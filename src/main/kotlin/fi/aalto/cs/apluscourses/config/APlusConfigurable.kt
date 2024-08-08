package fi.aalto.cs.apluscourses.config

import com.intellij.credentialStore.OneTimeString
import com.intellij.ide.plugins.MultiPanel
import com.intellij.ide.plugins.newui.TabbedPaneHeaderComponent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.observable.util.isNull
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.intellij.ui.dsl.validation.Level
import com.intellij.util.application
import com.intellij.util.applyIf
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.services.TokenStorage
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.ui.TokenForm
import fi.aalto.cs.apluscourses.ui.overview.ResponsiveImagePanel
import icons.PluginIcons
import java.awt.Component
import javax.swing.JComponent

internal class APlusConfigurable(val project: Project) : Configurable, Configurable.TopComponentProvider,
    Configurable.NoScroll {

    private val originalLanguage = AtomicProperty<String>("")
    private val selectedLanguage = AtomicProperty<String>("")

    private var isModified = false

    private val passwordField = JBPasswordField()

    private val SETTINGS_TAB = 0
    private val ABOUT_TAB = 1

    private var myTabHeaderComponent: TabbedPaneHeaderComponent? = null
    override fun getCenterComponent(controller: Configurable.TopComponentController): Component = myTabHeaderComponent!!

    private var settingsPanel: DialogPanel? = null
    private val tokenForm = TokenForm(project)

    private var banner: ResponsiveImagePanel? = null
    override fun createComponent(): JComponent {
        val course = CourseManager.course(project)
        val t = CourseFileManager.getInstance(project).state.language ?: ""
        selectedLanguage.set(t)
        originalLanguage.set(t)
        selectedLanguage.afterChange { isModified = originalLanguage.get() != it }
        val l = course?.languages ?: emptyList()
        banner = ResponsiveImagePanel(icon = PluginIcons.A_PLUS_COURSES_BANNER, width = 200)
        settingsPanel = panel {
            group("Global Settings") {
                with(tokenForm) {
                    user()
                    token()
                    validation()
                }
                row("Assistant mode") {
                    checkBox("Enable assistant mode").applyToComponent {
                        isEnabled = false
                    }
                }
            }
            group("Course Settings") {
                row("Language") {
//                comboBox<String>(l)
                    segmentedButton(l) {
                        text = it
                    }.bind(selectedLanguage)
                }
            }
        }
        val myCardPanel: MultiPanel = object : MultiPanel() {
            override fun create(key: Int): JComponent {
                if (key == SETTINGS_TAB) {
                    return settingsPanel!!
                }
                if (key == ABOUT_TAB) {
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
        myTabHeaderComponent!!.addTab("Settings", null)
        myTabHeaderComponent!!.addTab("About", null)
        myCardPanel.select(SETTINGS_TAB, true)
        myTabHeaderComponent!!.setListener()
        myTabHeaderComponent!!.getComponent(1).isVisible = false
        return myCardPanel
    }

    private val A_COURSES_PLUGIN_PAGE = "https://plugins.jetbrains.com/plugin/13634-a-courses"
    private val A_PLUS_PAGE = "https://plus.cs.aalto.fi/"
    private val GITHUB_PAGE = "https://github.com/Aalto-LeTech/aplus-courses"

    private fun aboutTab() = panel {
        row {
            cell(banner!!)
        }
        row {
            label("Version: 4.0.0-beta1").bold()
        }
        row {
            text(
                MyBundle.message("ui.aboutDialog.description")
            )
        }
        row {
            browserLink(
                MyBundle.message("ui.aboutDialog.website"), A_COURSES_PLUGIN_PAGE
            )
        }
        row {
            browserLink(
                MyBundle.message("ui.aboutDialog.GithubWebsite"), GITHUB_PAGE
            )
        }
        row {
            browserLink(
                MyBundle.message("ui.aboutDialog.APlusWebsite"), A_PLUS_PAGE
            )
        }
        row {
            text(
                MyBundle.message("ui.aboutDialog.authors")
            )
        }
        row {
            text(
                MyBundle.message("ui.aboutDialog.attributes")
            )
        }
    }

    override fun isModified(): Boolean = isModified || tokenForm.isModified

    override fun apply() {
//        CourseFileManager.getInstance(project).updateSettings(selectedLanguage)
        val originalLanguage = originalLanguage.get()
        val language = selectedLanguage.get()
        println("Selected language: $language")
        if (language.isNotEmpty() && originalLanguage != language) {
            CourseFileManager.getInstance(project).state.language = language
            CourseManager.getInstance(project).state.clearAll()
            ExercisesUpdaterService.getInstance(project).state.clearAll()
        }
        CourseManager.getInstance(project).restart()
        settingsPanel?.validationsOnApply?.values?.flatten()?.mapNotNull {
            it.validate()
        }
    }

    override fun reset() {
        selectedLanguage.set(originalLanguage.get())
        isModified = false
    }

    override fun getDisplayName(): String {
        return "A+ Courses"
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
    }
}
