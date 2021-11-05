package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public class BuildActionListener extends IdeActionListener {

  /**
   * Constructor.
   */
  public BuildActionListener(@NotNull ListenerCallback callback, @NotNull Project project,
                             @NotNull String actionName) {
    super(callback, project, actionName);
  }

  public static BuildActionListener create(ListenerCallback callback, Project project, @NotNull Arguments arguments) {
    return new BuildActionListener(callback, project, arguments.getString("actionName"));
  }

  @Override
  public void beforeActionPerformed(@NotNull AnAction action, @NotNull AnActionEvent event) {
    if (actionName.equals(action.getTemplateText())
        || actionName.equals(event.getPresentation().getText())) {
      check(true);
    }
  }
}
