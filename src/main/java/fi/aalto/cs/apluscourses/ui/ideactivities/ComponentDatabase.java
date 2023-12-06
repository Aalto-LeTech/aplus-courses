package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

public class ComponentDatabase {
  public static final String APLUS_TOOL_WINDOW = "A+ Courses";

  /**
   * Opens a tool window.
   *
   * @param id      The name of the tool window
   * @param project The project
   */
  public static void showToolWindow(@NotNull String id, @NotNull Project project) {
    var toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id);
    if (toolWindow != null) {
      try {
        toolWindow.activate(null);
      } catch (IllegalStateException e) {
        // do nothing
      }
    }
  }

  private ComponentDatabase() {

  }
}
