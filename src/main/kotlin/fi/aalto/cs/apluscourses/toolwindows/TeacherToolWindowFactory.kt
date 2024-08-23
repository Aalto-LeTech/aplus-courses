package fi.aalto.cs.apluscourses.toolwindows

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.MyBundle.message
import org.jetbrains.annotations.NonNls
import javax.swing.ScrollPaneConstants

internal class TeacherToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val ui = panel {
            collapsibleGroup(message("toolwindows.TeacherToolWindowFactory.courseInformation")) {
                row(message("toolwindows.TeacherToolWindowFactory.courseId")) {
                    textField()
                }
                row(message("toolwindows.TeacherToolWindowFactory.courseName")) {
                    textField().comment(message("toolwindows.TeacherToolWindowFactory.courseNameComment"))
                }
                row(message("toolwindows.TeacherToolWindowFactory.aPlusUrl")) {
                    textField().applyToComponent {
                        text = "https://plus.cs.aalto.fi/"
                    }
                }
            }.apply { expanded = true }
            collapsibleGroup(message("toolwindows.TeacherToolWindowFactory.resourceUrls")) {
                row(message("toolwindows.TeacherToolWindowFactory.ideSettings")) {
                    textField().comment(
                        message("toolwindows.TeacherToolWindowFactory.ideSettingsComment"),
                        50
                    )
                }
                row(message("toolwindows.TeacherToolWindowFactory.forMac")) {
                    textField()
                }
                row(message("toolwindows.TeacherToolWindowFactory.forLinux")) {
                    textField()
                }
                row(message("toolwindows.TeacherToolWindowFactory.forWindows")) {
                    textField()
                }
                row(message("toolwindows.TeacherToolWindowFactory.feedbackCss")) {
                    textField()
                }
            }.apply { expanded = true }
            collapsibleGroup(message("toolwindows.TeacherToolWindowFactory.modules")) {
                row(message("toolwindows.TeacherToolWindowFactory.baseUrl")) {
                    textField()
                }
                row(message("toolwindows.TeacherToolWindowFactory.namePattern")) {
                    textField().applyToComponent {
                        @NonNls val default = "{name}.zip"
                        text = default
                    }
                }
                row(message("toolwindows.TeacherToolWindowFactory.authentication")) {
                    checkBox(message("toolwindows.TeacherToolWindowFactory.authenticationCheckbox"))
                }
                row(message("toolwindows.TeacherToolWindowFactory.callbacks")) {
                    checkBox("AddModuleWatermark")
                }
                separator()
                row(message("toolwindows.TeacherToolWindowFactory.moduleName")) {
                    textField().applyToComponent {
                        text = ""
                    }
                }
                row(message("toolwindows.TeacherToolWindowFactory.moduleVersion")) {
                    textField().applyToComponent {
                        text = "1.2"
                    }
                }
                row(message("toolwindows.TeacherToolWindowFactory.moduleChangelog")) {
                    textField().applyToComponent {
                        text = ""
                    }
                }
                separator()
                row(message("toolwindows.TeacherToolWindowFactory.moduleName")) {
                    textField().applyToComponent {
                        text = ""
                    }
                }
                row(message("toolwindows.TeacherToolWindowFactory.moduleVersion")) {
                    textField().applyToComponent {
                        text = "1.0"
                    }
                }
                row(message("toolwindows.TeacherToolWindowFactory.moduleChangelog")) {
                    textField()
                }
                separator()
            }.apply { expanded = true }

        }
        val scrollPane = JBScrollPane(ui)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        val content = ContentFactory.getInstance().createContent(
            scrollPane,
            message("toolwindows.TeacherToolWindowFactory.tabTitle"), true
        )
        toolWindow.contentManager.addContent(content)
    }
}