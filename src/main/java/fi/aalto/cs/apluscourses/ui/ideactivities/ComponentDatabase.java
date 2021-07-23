package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.ToolWindowManager;
import java.awt.Component;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentDatabase {
  public static @Nullable Component getProjectPane() {
    Component component = ComponentLocator.getComponentByClass("ProjectViewPane");
    return component == null ? null : component.getParent();
  }

  /**
   * Returns any of the open editors - it is not deterministic which one will be returned.
   */
  public static @Nullable EditorComponentImpl getEditorWindow() {
    return (EditorComponentImpl) ComponentLocator.getComponentByClass("EditorComponentImpl");
  }

  /**
   * Out of all open editors, retrieves the one with a specific file open.
   */
  public static @Nullable EditorComponentImpl getEditorWindow(@NotNull Project project,
                                                              @NotNull String path) {
    var basePath = project.getBasePath();
    if (basePath == null) {
      return null;
    }
    var fullPath = Path.of(basePath, path);
    var editors = ComponentLocator.getComponentsByClass("EditorComponentImpl");
    for (var editorComponent : editors) {
      var editor = (EditorComponentImpl) editorComponent;
      if (editor.getEditor().getVirtualFile() != null
          && Path.of(editor.getEditor().getVirtualFile().getPath()).equals(fullPath)) {
        return editor;
      }
    }
    return null;
  }

  /**
   * Returns the button that controls the tutorial progress.
   *
   * @return A JButton or null, if no such a component was found.
   */
  public static @Nullable Component getProgressButton() {
    return ComponentLocator.getComponentByClass("TutorialProgressAction");
  }

  /**
   * Opens a file in the editor, return true if successful.
   */
  public static boolean showFile(@NotNull String path, @NotNull Project project) {
    if (project.getBasePath() == null) {
      return false;
    }
    var modulePath = Paths.get(project.getBasePath()).resolve(path);
    var vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
    if (vf == null) {
      return false;
    }
    new OpenFileDescriptor(project, vf).navigate(true);
    return true;
  }

  public static boolean showProjectToolWindow(@NotNull Project project) {
    return showToolWindow("Project", project);
  }

  public static boolean showAPlusToolWindow(@NotNull Project project) {
    return showToolWindow("A+ Courses", project);
  }

  private static boolean showToolWindow(@NotNull String id, @NotNull Project project) {
    var toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id);
    if (toolWindow != null) {
      try {
        toolWindow.activate(null);
        return true;
      } catch (IllegalStateException e) {
        return false;
      }
    }
    return false;
  }

  private ComponentDatabase() {

  }
}
