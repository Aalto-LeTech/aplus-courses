package fi.aalto.cs.apluscourses.toolwindows

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import javax.swing.ScrollPaneConstants

internal class TeacherToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val ui = panel {
            collapsibleGroup("Course Information") {
                row("A+ Course ID") {
                    textField()
                }
                row("Course Name") {
                    textField().comment("Shown in the tool window")
                }
                row("A+ URL") {
                    textField().applyToComponent {
                        text = "https://plus.cs.aalto.fi/"
                    }
                }
            }.apply { expanded = true }
            collapsibleGroup("Resource URLs") {
                row("IDE Settings") {
                    textField().comment(
                        "A .zip file, containing settings for IntelliJ that the user may optionally install while turning their project into an A+ project.",
                        50
                    )
                }
                row("   for macOS") {
                    textField()
                }
                row("   for Linux") {
                    textField()
                }
                row("   for Windows") {
                    textField()
                }
                row("Feedback CSS") {
                    textField()
                }
            }.apply { expanded = true }
            collapsibleGroup("Modules") {
                row("Base URL") {
                    textField()
                }
                row("Name Pattern") {
                    textField().applyToComponent {
                        text = "{name}.zip"
                    }
                }
                row("Authentication") {
                    checkBox("Require authentication for modules")
                }
                row("Installation Callbacks") {
                    checkBox("AddModuleWatermark")
                }
                separator()
                row("Name") {
                    textField().applyToComponent {
                        text = "Expressions"
                    }
                }
                row("Version") {
                    textField().applyToComponent {
                        text = "1.2"
                    }
                }
                row("Changelog") {
                    textField().applyToComponent {
                        text = "Fixed expressiontest"
                    }
                }
                separator()
                row("Name") {
                    textField().applyToComponent {
                        text = "Grep"
                    }
                }
                row("Version") {
                    textField().applyToComponent {
                        text = "1.0"
                    }
                }
                row("Changelog") {
                    textField()
                }
                separator()
            }.apply { expanded = true }

        }
        val scrollPane = JBScrollPane(ui)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        val content = ContentFactory.getInstance().createContent(scrollPane, "Config Generator", true)
        toolWindow.contentManager.addContent(content)
    }
}