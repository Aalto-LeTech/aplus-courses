package fi.aalto.cs.apluscourses.config

import com.intellij.credentialStore.OneTimeString
import com.intellij.credentialStore.askPassword
import com.intellij.ide.plugins.MultiPanel
import com.intellij.ide.plugins.newui.TabbedPaneHeaderComponent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.COLUMNS_SHORT
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.util.minimumWidth
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.dal.TokenStorage
import fi.aalto.cs.apluscourses.ui.overview.ResponsiveImagePanel
import icons.PluginIcons
import java.awt.Component
import javax.swing.JComponent

internal class APlusConfigurable(val project: Project) : Configurable, Configurable.TopComponentProvider,
    Configurable.NoScroll {
    private fun setToken() {
        TokenStorage.store(OneTimeString(passwordField.password))
        passwordField.text = ""
    }

    private val passwordField = JBPasswordField()

    private val SETTINGS_TAB = 0
    private val ABOUT_TAB = 1

    private var myTabHeaderComponent: TabbedPaneHeaderComponent? = null
    override fun getCenterComponent(controller: Configurable.TopComponentController): Component = myTabHeaderComponent!!

    private var banner: ResponsiveImagePanel? = null
    override fun createComponent(): JComponent {
        banner = ResponsiveImagePanel(icon = PluginIcons.A_PLUS_COURSES_BANNER, width = 200)
        val myCardPanel: MultiPanel = object : MultiPanel() {
            override fun create(key: Int): JComponent {
                if (key == SETTINGS_TAB) {
                    return panel {
                        row("Course language") {
                            comboBox<String>(listOf("English", "Finnish")).applyToComponent {
                                isEnabled = false
                            }
                        }
                        row("A+ token") {
                            cell(passwordField).columns(COLUMNS_MEDIUM)
                            button("Set") { setToken() }
                        }
                        row("Assistant mode") {
                            checkBox("Enable assistant mode").applyToComponent {
                                isEnabled = false
                            }
                        }
                    }
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

    override fun isModified(): Boolean {
        return false
    }

    override fun apply() {
    }

    override fun getDisplayName(): String {
        return "A+ Courses"
    }
}
