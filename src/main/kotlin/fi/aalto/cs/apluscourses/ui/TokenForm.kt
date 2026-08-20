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
import com.intellij.ui.dsl.builder.COLUMNS_TINY
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.MAX_LINE_LENGTH_WORD_WRAP
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.RowsRange
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
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
        val field = passwordField ?: return
        checking.set(true)
        TokenStorage.getInstance().storeAndCheck(
            OneTimeString(field.password.filter { it.isLetterOrDigit() }.toCharArray()),
            project
        ) { checkedUser ->
            user.set(checkedUser)
            userName.set(checkedUser?.userName ?: "")
            tokenFailed.set(checkedUser == null)
            if (checkedUser != null) {
                isModified = true
                val state = CourseManager.getInstance(project).state
                state.authenticated = true
                state.user = checkedUser
                callback()
            }
            // Must stay true until the callback has left this screen.
            checking.set(false)
        }
    }

    private fun removeToken() {
        TokenStorage.getInstance().remove()
        user.set(null)
        userName.set("")
        isModified = true
    }

    init {
        refresh()
    }

    fun refresh() {
        val refreshed = CourseManager.user(project)
        if (refreshed != user.get()) {
            tokenFailed.set(false)
        }
        user.set(refreshed)
        userName.set(refreshed?.userName ?: "")
    }

    fun Panel.user(): Row =
        row(message("ui.TokenForm.loggedInAs")) {
            text("").bindText(userName)
            button(message("ui.TokenForm.logOut")) { removeToken() }
        }.visibleIf(user.isNotNull())

    /** [compact] stacks the label, the field and the buttons on rows of their own. */
    fun Panel.token(compact: Boolean = false): RowsRange =
        rowsRange {
            if (compact) {
                row { tokenField(compact = true) }
                row { setButtons() }
            } else {
                row(message("ui.TokenForm.token")) {
                    tokenField()
                    setButtons()
                }
            }
        }.visibleIf(user.isNull())

    private fun Row.tokenField(compact: Boolean = false) {
        val aplusUrl = CourseManager.getInstance(project).state.aPlusUrl ?: "https://plus.cs.aalto.fi/"
        val field = passwordField()
            .columns(COLUMNS_TINY)
            .applyToComponent {
                passwordField = this
                addActionListener {
                    setToken()
                }
            }
            .resizableColumn()
            .align(AlignX.FILL)
            .enabledIf(checking.not())
            .comment(message("ui.TokenForm.tokenLink", aplusUrl), MAX_LINE_LENGTH_WORD_WRAP)
        if (compact) {
            field.label(message("ui.TokenForm.token"), LabelPosition.TOP)
        }
    }

    private fun Row.setButtons() {
        button(message("ui.TokenForm.setToken")) { setToken() }.visibleIf(checking.not())
        button(message("ui.TokenForm.checking")) {}.enabled(false).visibleIf(checking)
    }

    fun Panel.validation(): Row =
        row("") {
            text(message("ui.TokenForm.tokenError")).visibleIf(tokenFailed).applyToComponent {
                foreground = JBUI.CurrentTheme.NotificationError.borderColor()
            }
        }
}