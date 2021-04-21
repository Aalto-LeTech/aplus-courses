package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.ui.ideactivities.OverlayPane;
import org.jetbrains.annotations.NotNull;

/**
 * Please ignore this class in potential PR reviews. This class will not make it to the release
 * since it's only for demoing purposes.
 */
public class OverlayRemoveAction extends DumbAwareAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    OverlayPane.removeOverlay();
  }
}
