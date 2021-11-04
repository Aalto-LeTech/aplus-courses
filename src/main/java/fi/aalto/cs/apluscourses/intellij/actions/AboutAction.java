package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.diagnostic.VMOptions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.ui.AboutDialog;
import org.jetbrains.annotations.NotNull;

public class AboutAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (VMOptions.canWriteOptions()) {
      VMOptions.writeOption("console.encoding", "=", "UTF-8");
    }

    AboutDialog.display();
  }
}
