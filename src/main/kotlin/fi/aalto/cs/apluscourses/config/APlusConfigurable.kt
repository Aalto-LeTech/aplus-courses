package fi.aalto.cs.apluscourses.config

import com.intellij.credentialStore.OneTimeString
import com.intellij.credentialStore.askPassword
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.dal.TokenStorage
import javax.swing.JComponent

class APlusConfigurable(val project: Project) : Configurable {
    internal data class Model(
        var token: String = ""
    )

    private val model = Model()

    private fun setToken() {
        println("setting ${model.token}")
        val pass = askPassword(project, "token", "gib token", TokenStorage.credentialAttributes) ?: return
        TokenStorage.store(OneTimeString(pass))
        model.token = ""

    }

    override fun createComponent(): JComponent {
        return panel {
            row("Course language") {
                comboBox<String>(listOf("English", "Finnish"))
            }
            row("A+ token") {
                passwordField().bindText(model::token)
                button("Set") { setToken() }
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
