package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.ui.ideactivities.OverlayPane;
import org.jetbrains.annotations.NotNull;

public class OverlayInstallAction extends DumbAwareAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    OverlayPane.installOverlay();
  }
}
