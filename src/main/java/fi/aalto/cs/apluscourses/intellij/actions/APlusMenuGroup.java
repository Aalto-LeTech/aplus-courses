package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;

public class APlusMenuGroup extends DefaultActionGroup {

  @Override
  public void update(@NotNull AnActionEvent e) {
    System.out.println("inside update...");
    e.getPresentation().setEnabledAndVisible(true);
    super.update(e);
  }
}
