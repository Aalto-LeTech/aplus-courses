package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class Background(
    val cs: CoroutineScope
) {
    fun runInBackground(action: suspend () -> Unit) {
        cs.launch {
            action()
        }
    }
}
