package fi.aalto.cs.apluscourses.ui.ideactivities;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentLocator {
  public static @Nullable Component getComponentByClass(@NotNull String componentClassSubstring) {
    return getComponentByClass(componentClassSubstring, JOptionPane.getRootFrame());
  }

  private static @Nullable Component getComponentByClass(@NotNull String componentClassSubstring,
                                                         @NotNull Container parentComponent) {
    for (var component : parentComponent.getComponents()) {
      if (component.getClass().toString().contains(componentClassSubstring)) {
        return component;
      }

      // if a component is a Container, then it can contain more components and we should scan them
      if (component instanceof Container) {
        var checkResult = getComponentByClass(componentClassSubstring, (Container) component);
        if (checkResult != null) {
          return checkResult;
        }
      }
    }

    return null;
  }
}
