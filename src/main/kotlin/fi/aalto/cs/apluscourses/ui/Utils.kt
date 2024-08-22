package fi.aalto.cs.apluscourses.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.AnActionLink
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import java.awt.event.ActionEvent
import javax.swing.Icon

object Utils {
    fun loadingPanel(): DialogPanel {
        return panel {
            row {
                icon(CoursesIcons.Loading).resizableColumn().align(Align.CENTER)
            }.resizableRow()
        }
    }

    fun Row.myLink(text: String, icon: Icon, action: (ActionEvent) -> Unit) =
        link(text, action).applyToComponent {
            setIcon(icon, false)
        }

    fun Row.myActionLink(text: String, icon: Icon, action: AnAction) =
        cell(AnActionLink(text, action)).applyToComponent {
            setIcon(icon, false)
        }
}