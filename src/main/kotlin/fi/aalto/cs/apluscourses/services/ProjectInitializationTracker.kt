package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

@Service(Service.Level.PROJECT)
class ProjectInitializationTracker(val project: Project, val cs: CoroutineScope) {
    private val initializationTasks: MutableList<Deferred<Boolean>> = mutableListOf()

    fun addInitializationTask(task: Deferred<Boolean>) {
        initializationTasks.add(task)
    }

    suspend fun waitForAllTasks() {
        initializationTasks.awaitAll()
    }
}