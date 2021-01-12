package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.ui.AboutDialog;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class AboutAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    try {
      AboutDialog.display();
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }
}
