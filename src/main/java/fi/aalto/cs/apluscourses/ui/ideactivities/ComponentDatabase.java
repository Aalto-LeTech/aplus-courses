package fi.aalto.cs.apluscourses.ui.ideactivities;

import java.awt.Component;
import javax.swing.JButton;
import org.jetbrains.annotations.Nullable;

public class ComponentDatabase {
  public static @Nullable Component getProjectPane() {
    Component component = ComponentLocator.getComponentByClass("ProjectViewPane");
    return component == null ? null : component.getParent();
  }

  public static @Nullable Component getEditorWindow() {
    return ComponentLocator.getComponentByClass("EditorWindow");
  }

  public static @Nullable Component getProgressButton() {
    return ComponentLocator.getComponentsByClass("JButton")
        .stream().filter(c -> c instanceof JButton)
        .filter(c -> ComponentLocator.hasActionOfClass((JButton) c, "ProgressAction"))
        .findFirst().orElse(null);
  }

  private ComponentDatabase() {

  }
}
