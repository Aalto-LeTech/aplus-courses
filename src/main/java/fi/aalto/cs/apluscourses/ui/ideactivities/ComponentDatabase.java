package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.wm.ToolWindowManager;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import java.awt.Component;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentDatabase {
  public static final String PROJECT_TOOL_WINDOW = "Project";
  public static final String APLUS_TOOL_WINDOW = "A+ Courses";
  public static final String RUN_TOOL_WINDOW = "Run";

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

  public static @Nullable ActionToolbarImpl getNavBarToolBar() {
    return ActionToolbarImpl
        .findToolbar((ActionGroup) CustomActionsSchema.getInstance().getCorrectedAction("NavBarToolBar"));
  }

  /**
   * Opens a file in the editor, return true if successful.
   */
  public static boolean showFile(@NotNull String path, @NotNull Project project) {
    if (project.getBasePath() == null || project.isDisposed()) {
      return false;
    }
    var modulePath = Path.of(project.getBasePath(), path);
    var vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
    if (vf == null) {
      return false;
    }
    new OpenFileDescriptor(project, vf).navigate(true);
    return true;
  }

  /**
   * Opens a tool window.
   * @param id      The name of the tool window
   * @param project The project
   */
  public static boolean showToolWindow(@NotNull String id, @NotNull Project project) {
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

  /**
   * Closes file in the editor for a given project.
   */
  public static void closeFile(@NotNull Arguments actionArguments, @NotNull Project project) {
    var filePath = (String) actionArguments.opt("filePath");
    if (filePath == null) {
      return;
    }
    var modulePath = Path.of(project.getBasePath(), filePath);
    var vf = LocalFileSystem.getInstance().findFileByIoFile(modulePath.toFile());
    if (vf == null) {
      return;
    }
    FileEditorManager.getInstance(project).closeFile(vf);
  }

  /**
   * Closes a tool window.
   * @param id      The name of the tool window
   * @param project The project
   */
  public static boolean hideToolWindow(@NotNull String id, @NotNull Project project) {
    var toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id);
    if (toolWindow != null) {
      try {
        toolWindow.hide();
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
