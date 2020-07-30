package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

import fi.aalto.cs.apluscourses.intellij.dal.IntelliJPasswordStorage;
import fi.aalto.cs.apluscourses.presentation.commands.APlusAuthenticationCommand;
import fi.aalto.cs.apluscourses.presentation.commands.MainViewModelContext;
import org.jetbrains.annotations.NotNull;

public class APlusAuthenticationAction extends CommandAction<MainViewModelContext>
    implements DumbAware {

  public static final String ACTION_ID = APlusAuthenticationAction.class.getCanonicalName();

  public APlusAuthenticationAction() {
    super(new APlusAuthenticationCommand(IntelliJPasswordStorage::new));
  }

  @NotNull
  @Override
  protected MainViewModelContext getContext(@NotNull AnActionEvent e) {
    return new MainViewModelContextImpl(e);
  }
}
