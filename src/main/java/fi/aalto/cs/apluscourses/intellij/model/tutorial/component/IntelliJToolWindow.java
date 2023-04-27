package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.util.Optional;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJToolWindow extends IntelliJTutorialComponent<JComponent> {
  private final @NotNull String toolWindowId;

  public IntelliJToolWindow(@NotNull String toolWindowId,
                            @Nullable TutorialComponent parent,
                            @Nullable Project project) {
    super(parent, project);
    this.toolWindowId = toolWindowId;
  }

  @Override
  protected @Nullable JComponent getAwtComponent() {
    return Optional.ofNullable(getProject())
        .map(ToolWindowManager::getInstance)
        .map(this::getToolWindow)
        .filter(ToolWindow::isVisible)
        .map(ToolWindow::getComponent)
        .orElse(null);
  }

  private @Nullable ToolWindow getToolWindow(@NotNull ToolWindowManager manager) {
    return manager.getToolWindow(toolWindowId);
  }
}
