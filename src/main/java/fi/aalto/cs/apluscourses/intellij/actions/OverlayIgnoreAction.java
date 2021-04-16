package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentLocator;
import fi.aalto.cs.apluscourses.ui.ideactivities.OverlayPane;
import java.awt.Component;
import org.jetbrains.annotations.NotNull;


public class OverlayIgnoreAction extends DumbAwareAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var projectPane = ComponentLocator.getComponentByClass("ProjectViewPane");
    if (projectPane != null) {
      // ProjectViewPane may extend into an area that is not drawn, because the project pane
      // may be resized by the user. Its parent is a Viewport class, which represents only the
      // visible part of the project pane.
      Component c = projectPane.getParent();

      OverlayPane.showComponent(c);
      OverlayPane.addPopup(c, "Overlay example", "Example text for the popup message ".repeat(15));
    }
  }
}
