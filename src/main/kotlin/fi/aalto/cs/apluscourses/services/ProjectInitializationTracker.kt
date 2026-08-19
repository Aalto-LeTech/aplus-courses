package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class ProjectInitializationTracker(val project: Project, val cs: CoroutineScope) {
    private val initializationTasks: MutableList<Deferred<Boolean>> = mutableListOf()

    fun addInitializationTask(task: suspend () -> Unit) {
        val deferred = CompletableDeferred<Boolean>()
        initializationTasks.add(deferred)
        cs.launch {
            try {
                task()
            } finally {
                deferred.complete(true)
            }
        }
    }

    suspend fun waitForAllTasks() {
        initializationTasks.awaitAll()
    }
}