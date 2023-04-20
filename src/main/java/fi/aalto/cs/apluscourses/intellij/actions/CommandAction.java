package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.IntelliJContext;
import fi.aalto.cs.apluscourses.presentation.Command;
import fi.aalto.cs.apluscourses.presentation.base.PresentationContext;
import org.jetbrains.annotations.NotNull;

public class CommandAction extends DumbAwareAction {

  private final @NotNull Command command;

  public CommandAction(@NotNull Command command) {
    this.command = command;
  }

  private @NotNull PresentationContext createContext(@NotNull AnActionEvent e) {
    return IntelliJContext.DEFAULT.withProject(e.getProject()); //TODO: cache contexts for projects
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(command.canExecute(createContext(e)));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    command.execute(createContext(e));
  }
}
