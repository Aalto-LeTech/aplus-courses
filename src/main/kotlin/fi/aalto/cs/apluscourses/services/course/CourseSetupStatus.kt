package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.application
import com.intellij.util.messages.Topic
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

enum class SetupStepState {
    PENDING,

    RUNNING,
    DONE,

    ACTION_NEEDED,

    FAILED,
}

/** What the student can do about a step that is waiting on them. */
enum class SetupAction {
    /** Downloads the course's missing plugins. */
    INSTALL_PLUGINS,

    /**
     * Opens the plugin manager so a disabled plugin can be switched back on.
     */
    OPEN_PLUGIN_SETTINGS,

    RESTART_IDE,
}

data class SetupStep(
    @param:NonNls val id: String,
    @param:Nls val title: String,
    val state: SetupStepState,
    @param:Nls val detail: String? = null,
    val action: SetupAction? = null,
    val detailIsMeasurement: Boolean = false, // Measurements are shown in a monospace font
)

/**
 * What still has to happen before a course is ready.
 */
@Service(Service.Level.PROJECT)
class CourseSetupStatus(private val project: Project) {
    private val lock = Any()
    private val steps = LinkedHashMap<String, SetupStep>()

    private var busySince: Long? = null

    val visibleSteps: List<SetupStep>
        get() = synchronized(lock) { steps.values.toList() }

    val isFinished: Boolean
        get() = synchronized(lock) {
            steps.values.all { it.state == SetupStepState.DONE }
        }

    /**
     * Whether unfinished work has lasted longer than [SHOW_AFTER_MS].
     */
    val isWorthShowing: Boolean
        get() = synchronized(lock) {
            val since = busySince ?: return false
            System.currentTimeMillis() - since >= SHOW_AFTER_MS
        }

    fun report(
        @NonNls id: String,
        @Nls title: String,
        state: SetupStepState,
        @Nls detail: String? = null,
        action: SetupAction? = null,
        detailIsMeasurement: Boolean = false,
    ) {
        val changed = synchronized(lock) {
            val updated = SetupStep(id, title, state, detail, action, detailIsMeasurement)
            val previous = steps.put(id, updated)
            refreshBusySince()
            previous != updated
        }
        if (changed) fireChanged()
    }

    fun update(
        @NonNls id: String,
        state: SetupStepState,
        @Nls detail: String? = null,
        action: SetupAction? = null,
        detailIsMeasurement: Boolean = false,
    ) {
        val changed = synchronized(lock) {
            val existing = steps[id] ?: return
            val updated = existing.copy(
                state = state,
                detail = detail,
                action = action,
                detailIsMeasurement = detailIsMeasurement
            )
            steps[id] = updated
            refreshBusySince()
            existing != updated
        }
        if (changed) fireChanged()
    }

    /** Must be called while holding [lock]. */
    private fun refreshBusySince() {
        val busy = steps.values.any { it.state != SetupStepState.DONE }
        busySince = when {
            !busy -> null
            busySince == null -> System.currentTimeMillis()
            else -> busySince
        }
    }

    fun clear() {
        val hadAny = synchronized(lock) { busySince = null; steps.isNotEmpty().also { steps.clear() } }
        if (hadAny) fireChanged()
    }

    private fun fireChanged() {
        application.invokeLater {
            if (project.isDisposed) return@invokeLater
            project.messageBus.syncPublisher(TOPIC).onSetupChanged()
        }
    }

    fun interface SetupListener {
        fun onSetupChanged()
    }

    companion object {
        val TOPIC: Topic<SetupListener> =
            Topic(SetupListener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        fun getInstance(project: Project): CourseSetupStatus = project.service()

        @NonNls
        const val CONFIGURATION: String = "configuration"

        @NonNls
        const val PLUGINS: String = "plugins"

        @NonNls
        const val SETTINGS: String = "settings"

        @NonNls
        const val JDK: String = "jdk"

        /** How long work must persist before the checklist is worth showing. */
        private const val SHOW_AFTER_MS = 400L

        @NonNls
        fun moduleStep(name: String): String = "module:$name"
    }
}
