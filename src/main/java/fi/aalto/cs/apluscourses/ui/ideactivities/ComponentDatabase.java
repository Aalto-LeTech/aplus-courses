package fi.aalto.cs.apluscourses.ui.ideactivities;

import java.awt.Component;
import org.jetbrains.annotations.Nullable;

public class ComponentDatabase {
  public static @Nullable Component getProjectPane() {
    Component component = ComponentLocator.getComponentByClass("ProjectViewPane");
    return component == null ? null : component.getParent();
  }

  public static @Nullable Component getEditorWindow() {
    return ComponentLocator.getComponentByClass("EditorWindow");
  }

  private ComponentDatabase() {

  }
}
