package fi.aalto.cs.apluscourses.intellij.utils.startup;

import com.intellij.ide.startup.StartupActionScriptManager.ActionCommand;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

public abstract class ActionCommandDecorator implements ActionCommand, Serializable {

  private static final long serialVersionUID = -1176927881625341960L;

  @NotNull
  private final Serializable actionCommand;

  public ActionCommandDecorator(@NotNull ActionCommand actionCommand) {
    if (!(actionCommand instanceof Serializable)) {
      throw new IllegalArgumentException();
    }
    this.actionCommand = (Serializable) actionCommand;
  }

  @Override
  public abstract void execute();

  protected ActionCommand getActionCommand() {
    return (ActionCommand) actionCommand;
  }
}
