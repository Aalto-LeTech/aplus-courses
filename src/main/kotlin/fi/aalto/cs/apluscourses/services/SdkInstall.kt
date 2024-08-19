package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.io.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
class SdkInstall(val project: Project, val cs: CoroutineScope) {
    var sdkDownloadedFuture: CompletableFuture<Boolean>? = null
    fun setFuture(sdkDownloadedFuture: CompletableFuture<Boolean>) {
        this.sdkDownloadedFuture = sdkDownloadedFuture
    }

    fun waitForInstall(afterInstall: suspend () -> Unit = {}) {
        cs.launch {
            sdkDownloadedFuture?.await()
            afterInstall()
        }
    }
}