package fi.aalto.cs.apluscourses.ui

import com.intellij.credentialStore.OneTimeString
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.observable.util.isNull
import com.intellij.openapi.observable.util.not
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.bindText
import com.intellij.util.ui.JBUI
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.services.TokenStorage
import fi.aalto.cs.apluscourses.services.course.CourseManager

class TokenForm(private val project: Project, private val callback: () -> Unit = {}) {
    val checking = AtomicBooleanProperty(false)
    val tokenFailed = AtomicBooleanProperty(false)
    val user = AtomicProperty<User?>(null)
    val userName = AtomicProperty<String>("")
    var isModified = false
    private var passwordField: JBPasswordField? = null

    private fun setToken() {
        checking.set(true)
        TokenStorage.getInstance().storeAndCheck(
            OneTimeString(passwordField!!.password.filter { it.isLetterOrDigit() }.toCharArray()),
            project
        ) {
            user.set(it)
            userName.set(it?.userName ?: "")
            checking.set(false)
            tokenFailed.set(it == null)
            if (it != null) {
                isModified = true
                callback()
            }
        }
        passwordField!!.text = ""
    }

    private fun removeToken() {
        TokenStorage.getInstance().remove()
        user.set(null)
        userName.set("")
        isModified = true
    }

    init {
        user.set(CourseManager.user(project))
        userName.set(user.get()?.userName ?: "")
    }

    fun Panel.user(): Row =
        row("Logged in as") {
            text("").bindText(userName)
            button("Log Out") { removeToken() }
        }.visibleIf(user.isNotNull())

    fun Panel.token(): Row =
        row("A+ token") {
            passwordField()
                .applyToComponent {
                    passwordField = this
                }
                .resizableColumn()
                .align(AlignX.FILL)
                .comment("<a href=\"https://plus.cs.aalto.fi/accounts/accounts/\">What is my token?</a>")
            button("Set") { setToken() }.visibleIf(checking.not())
            button("Checking...") {}.enabled(false).visibleIf(checking)
        }.visibleIf(user.isNull())

    fun Panel.validation(): Row =
        row("") {
            text("Token is invalid").visibleIf(tokenFailed).applyToComponent {
                foreground = JBUI.CurrentTheme.NotificationError.borderColor()
            }
        }
}