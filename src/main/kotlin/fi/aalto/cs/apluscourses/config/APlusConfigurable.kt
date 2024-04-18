package fi.aalto.cs.apluscourses.config

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class APlusConfigurable : Configurable {
    override fun createComponent(): JComponent {
        return panel {
            row("Course language") {
                comboBox<String>(listOf("English", "Finnish"))
            }
            row("A+ token") {
                passwordField()
                button("Set", { println("Set token") })
            }
            row("Assistant mode") {
                checkBox("Enable assistant mode")
            }
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
