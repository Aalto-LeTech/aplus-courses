package fi.aalto.cs.apluscourses.utils.callbacks

import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.utils.callbacks.Callbacks.PostDownloadModuleCallback
import fi.aalto.cs.apluscourses.api.CourseConfig

class Callbacks private constructor(private val postDownloadModuleCallbacks: List<PostDownloadModuleCallback>) {
    fun interface PostDownloadModuleCallback {
        fun postDownloadModule(project: Project, module: Module)
    }

    fun invokePostDownloadModuleCallbacks(project: Project, module: Module) {
        postDownloadModuleCallbacks.forEach { callback ->
            callback.postDownloadModule(
                project,
                module
            )
        }
    }

    companion object {
        private val availablePostDownloadModuleCallbacks: Map<String, PostDownloadModuleCallback> = mapOf(
            "AddModuleWatermark" to PostDownloadModuleCallback { project: Project, module: Module ->
                AddModuleWatermark.postDownloadModule(
                    project,
                    module
                )
            }
        )

        private fun <T> getCallbacksFromList(
            callbackArray: List<String>,
            sourceCallbacks: Map<String, T>
        ): List<T> = callbackArray.mapNotNull { sourceCallbacks[it] }

        fun fromJsonObject(callbacksObject: CourseConfig.Callbacks?): Callbacks {
            if (callbacksObject == null) {
                return Callbacks(emptyList())
            }

            val postDownloadModuleCallbacks = getCallbacksFromList(
                callbacksObject.postDownloadModule,
                availablePostDownloadModuleCallbacks
            )

            return Callbacks(postDownloadModuleCallbacks)
        }
    }
}
