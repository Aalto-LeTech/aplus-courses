package fi.aalto.cs.apluscourses.intellij.utils.startup;

import com.intellij.ide.startup.StartupActionScriptManager.ActionCommand;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class SilentActionCommandDecorator extends ActionCommandDecorator {

  private static final long serialVersionUID = 6933721646613676086L;

  public SilentActionCommandDecorator(@NotNull ActionCommand actionCommand) {
    super(actionCommand);
  }

  @Override
  public void execute() {
    try {
      getActionCommand().execute();
    } catch (IOException e) {
      // do nothing
    }
  }
}
