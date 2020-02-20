package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import fi.aalto.cs.apluscourses.ui.AboutDialog;
import org.jetbrains.annotations.NotNull;

public class AboutAction extends AnAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    AboutDialog.display();
  }
}
