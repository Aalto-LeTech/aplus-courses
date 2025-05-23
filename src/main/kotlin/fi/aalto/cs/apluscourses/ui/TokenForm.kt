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
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.services.TokenStorage
import fi.aalto.cs.apluscourses.services.course.CourseManager

class TokenForm(private val project: Project, private val callback: () -> Unit = {}) {
    private val checking: AtomicBooleanProperty = AtomicBooleanProperty(false)
    private val tokenFailed: AtomicBooleanProperty = AtomicBooleanProperty(false)
    val user: AtomicProperty<User?> = AtomicProperty(null)
    private val userName: AtomicProperty<String> = AtomicProperty("")
    var isModified: Boolean = false
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
        row(message("ui.TokenForm.loggedInAs")) {
            text("").bindText(userName)
            button(message("ui.TokenForm.logOut")) { removeToken() }
        }.visibleIf(user.isNotNull())

    fun Panel.token(): Row {
        val aplusUrl = CourseManager.getInstance(project).state.aPlusUrl ?: "https://plus.cs.aalto.fi/"
        return row(message("ui.TokenForm.token")) {
            passwordField()
                .applyToComponent {
                    passwordField = this
                    addActionListener {
                        setToken()
                    }
                }
                .resizableColumn()
                .align(AlignX.FILL)
                .comment(message("ui.TokenForm.tokenLink", aplusUrl))
            button(message("ui.TokenForm.setToken")) { setToken() }.visibleIf(checking.not())
            button(message("ui.TokenForm.checking")) {}.enabled(false).visibleIf(checking)
        }.visibleIf(user.isNull())
    }

    fun Panel.validation(): Row =
        row("") {
            text(message("ui.TokenForm.tokenError")).visibleIf(tokenFailed).applyToComponent {
                foreground = JBUI.CurrentTheme.NotificationError.borderColor()
            }
        }
}