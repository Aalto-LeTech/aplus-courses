package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.project.Project
import java.util.*

//import org.jetbrains.plugins.scala.console.ScalaConsoleInfo;
class ScalaReplObserver
/**
 * Instantiates a new ScalaReplObserver.
 *
 * @param project  Project.
 * @param module   Name of the module.
 * @param callback Who to call when the REPL is open.
 */(private val project: Project, private val module: String, private val callback: Callback) {
    private val timer = Timer()

    fun start() {
//    timer.schedule(new DelegateTimerTask(this::checkIsReplOpen), 500, 500);
    }

    fun stop() {
        timer.cancel()
    }

    private fun checkIsReplOpen() {
        if (isReplOpen) {
            stop()
            callback.onReplOpen()
        }
    }

    val isReplOpen: Boolean
        get() = isReplOpen(project, module)

    interface Callback {
        fun onReplOpen()
    }

    companion object {
        /**
         * Returns true if the REPL for a given module is open.
         */
        fun isReplOpen(project: Project, module: String): Boolean {
            return false //TODO
            //    return ScalaConsoleInfo.getConsole(project) != null
//        && ExecutionManagerImpl.getAllDescriptors(project).stream().anyMatch(d -> d.getDisplayName().contains("REPL")
//        && d.getDisplayName().contains(module));
        }
    }
}
