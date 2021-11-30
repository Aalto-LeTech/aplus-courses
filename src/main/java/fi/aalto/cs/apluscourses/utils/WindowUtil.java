package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

public class WindowUtil {
  private WindowUtil() {
  }

  /**
   * Brings the window to the front.
   */
  public static void bringWindowToFront(@NotNull Project project) {
    var frame = WindowManager.getInstance().getFrame(project);
    if (frame != null) {
      frame.toFront();
      frame.repaint();
    }
  }
}
