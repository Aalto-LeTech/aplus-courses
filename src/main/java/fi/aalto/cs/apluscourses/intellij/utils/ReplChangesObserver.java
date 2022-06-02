package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ReplChangesObserver {
  private static boolean documentListenerInstalled;
  private static final @NotNull Disposable disposable = Disposer.newDisposable();
  private static final Set<Module> modifiedModules = new HashSet<>();

  private ReplChangesObserver() {

  }

  /**
   * Triggered when a REPL has started for a particular module, indicating that all pending code changes
   * have been applied in the REPL as well.
   * @param module The module for which the REPL is being opened.
   */
  public static void onStartedRepl(@NotNull Module module) {
    if (!documentListenerInstalled) {
      EditorFactory.getInstance().getEventMulticaster().addDocumentListener(
          new ChangesListener(module.getProject()), disposable);
      documentListenerInstalled = true;
    }

    synchronized (modifiedModules) {
      modifiedModules.remove(module);
    }
  }

  /**
   * Triggered when a module undergoes some code change, which indicates that existing REPLs should
   * show a warning message that they're running an outdated version of the module.
   * @param module The module which has been changed.
   */
  public static void onModuleChanged(@NotNull Module module) {
    synchronized (modifiedModules) {
      modifiedModules.add(module);
    }
  }

  public static boolean hasModuleChanged(@NotNull Module module) {
    return modifiedModules.contains(module);
  }

  private static class ChangesListener implements DocumentListener {
    private final @NotNull Project project;

    public ChangesListener(@NotNull Project project) {
      this.project = project;
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
      VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());
      if (file == null) {
        return;
      }

      Module module = ProjectFileIndex.getInstance(project).getModuleForFile(file);
      if (module == null) {
        return;
      }

      ReplChangesObserver.onModuleChanged(module);
    }
  }
}
