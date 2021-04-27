package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentLocator;
import fi.aalto.cs.apluscourses.ui.ideactivities.OverlayPane;
import java.awt.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Please ignore this class in potential PR reviews. This class will not make it to the release
 * since it's only for demoing purposes.
 */
public class OverlayIgnoreAction extends DumbAwareAction {
  private static int operation = 0;

  private @Nullable Component getComponent() {
    Component component = null;

    switch (operation) {
      case 0:
        component = ComponentLocator.getComponentByClass("ProjectViewPane");
        return component != null ? component.getParent() : null;

      case 1:
        component = ComponentLocator.getComponentByClass("ModuleListView");
        return component != null ? component.getParent().getParent() : null;

      case 2:
        component = ComponentLocator.getButtonByActionClass("OpenItemAction");
        return component != null ? component.getParent().getParent() : null;

      case 3:
        component = ComponentLocator.getComponentByClass("TreeView");
        return component != null ? component.getParent().getParent() : null;

      default:
        operation = 0;
    }

    return null;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    OverlayPane.resetOverlay();

    Component component = getComponent();

    if (component != null) {
      OverlayPane.showComponent(component);
      OverlayPane.addPopup(component, "Overlay example", "Example text ".repeat(30));
      operation++;
    }
  }
}
