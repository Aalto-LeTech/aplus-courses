package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import fi.aalto.cs.apluscourses.ui.ideactivities.GenericHighlighter;
import javax.swing.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReplPresenter extends IntelliJComponentPresenterBase {
  private final Module module;

  /**
   * A constructor.
   */
  public ReplPresenter(@NotNull String instruction,
                       @NotNull String info,
                       @NotNull String module,
                       @NotNull Project project,
                       @NotNull Action @NotNull [] actions) throws IllegalArgumentException {
    super(instruction, info, project, actions);
    this.module = ModuleManager.getInstance(project).findModuleByName(module);
    if (this.module == null) {
      throw new IllegalArgumentException("Module not found: '" + module + "'");
    }
  }

  @NotNull
  public static ReplPresenter create(@NotNull String instruction,
                                     @NotNull String info,
                                     @NotNull Project project,
                                     @NotNull Arguments actionArguments,
                                     @NotNull Action @NotNull [] actions) {
    return new ReplPresenter(instruction, info, actionArguments.getString("module"), project, actions);
  }



  @Nullable
  @Override
  protected GenericHighlighter getHighlighter() {
    var toolWindow = ComponentDatabase.getToolWindow(ComponentDatabase.RUN_TOOL_WINDOW, project);
    return toolWindow == null || !toolWindow.isVisible() ? null : new GenericHighlighter(toolWindow.getComponent());
  }

  @Override
  protected boolean tryToShow() {
    return ComponentDatabase.startAndShowRepl(module, project);
  }
}
