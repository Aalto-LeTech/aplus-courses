package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Disposer
import java.util.*

object ReplChangesObserver {
    private var documentListenerInstalled = false
    private val disposable = Disposer.newDisposable()
    private val modifiedModules: MutableSet<Module> = Collections.synchronizedSet(HashSet())

    /**
     * Triggered when a REPL has started for a particular module, indicating that all pending code changes
     * have been applied in the REPL as well.
     * @param module The module for which the REPL is being opened.
     */
    fun onStartedRepl(module: Module) {
        if (!documentListenerInstalled) {
            EditorFactory.getInstance().eventMulticaster.addDocumentListener(
                ChangesListener(module.project), disposable
            )
            documentListenerInstalled = true
        }

        modifiedModules.remove(module)
    }

    /**
     * Triggered when a module undergoes some code change, which indicates that existing REPLs should
     * show a warning message that they're running an outdated version of the module.
     * @param module The module which has been changed.
     */
    fun onModuleChanged(module: Module) {
        modifiedModules.add(module)
    }

    fun hasModuleChanged(module: Module): Boolean {
        return modifiedModules.contains(module)
    }

    private class ChangesListener(private val project: Project) : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            val file = FileDocumentManager.getInstance().getFile(event.document) ?: return

            if (!project.isDisposed && project.isOpen) {
                val module = ProjectFileIndex.getInstance(project).getModuleForFile(file) ?: return

                onModuleChanged(module)
            }
        }
    }
}
