package fi.aalto.cs.apluscourses.intellij.model.task;

import static fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase.BUILD_TOOL_WINDOW;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import fi.aalto.cs.apluscourses.ui.ideactivities.GenericHighlighter;
import java.util.Optional;
import javax.swing.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildPresenter extends IntelliJComponentPresenterBase {
  public BuildPresenter(@Nullable String instruction,
                        @Nullable String info,
                        @NotNull Project project,
                        @NotNull Action @NotNull [] actions) {
    super(instruction, info, project, actions);
  }

  @Override
  protected @Nullable GenericHighlighter getHighlighter() {
    var toolWindow = getToolWindow();
    return toolWindow == null ? null : new GenericHighlighter(toolWindow.getComponent());
  }

  @Override
  public boolean tryToShow() {
    return ComponentDatabase.showToolWindow(BUILD_TOOL_WINDOW, project);
  }

  @Override
  public boolean isVisible() {
    return Optional.ofNullable(getToolWindow()).map(ToolWindow::isVisible).orElse(false);
  }

  @Nullable
  private ToolWindow getToolWindow() {
    return ComponentDatabase.getToolWindow(BUILD_TOOL_WINDOW, project);
  }
}
