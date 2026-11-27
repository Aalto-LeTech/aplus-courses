package fi.aalto.cs.apluscourses.ui

import com.intellij.openapi.application.EDT
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

/** Runs an action once things have settled, replacing whatever was scheduled before it. */
fun interface SettleScheduler {
    fun after(delayMillis: Long, action: () -> Unit)
}

/** One coroutine on the EDT, cancelled by the next request. */
class CoroutineSettleScheduler(private val scope: CoroutineScope) : SettleScheduler {
    private var pending: Job? = null

    override fun after(delayMillis: Long, action: () -> Unit) {
        pending?.cancel()
        pending = scope.launch(Dispatchers.EDT) {
            delay(delayMillis.milliseconds)
            action()
        }
    }
}
