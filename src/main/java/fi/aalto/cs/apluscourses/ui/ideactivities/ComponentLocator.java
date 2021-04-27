package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.openapi.actionSystem.AnActionHolder;
import java.awt.Component;
import java.awt.Container;
import java.util.function.Predicate;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentLocator {
  private static @Nullable Component getComponentByClass(@NotNull Container parentComponent,
                                                         @NotNull Predicate<Component> predicate) {
    for (var component : parentComponent.getComponents()) {
      if (predicate.test(component)) {
        return component;
      }

      // if a component is a Container, then it can contain more components and we should scan them
      if (component instanceof Container) {
        var checkResult = getComponentByClass((Container) component, predicate);
        if (checkResult != null) {
          return checkResult;
        }
      }
    }

    return null;
  }

  /**
   * Scans through all components and locates the first one which class name matches a substring.
   * @param componentClassSubstring A case-sensitive substring of the component's desired class.
   */
  public static @Nullable Component getComponentByClass(@NotNull String componentClassSubstring) {
    Predicate<Component> predicate =
        (c) -> c.getClass().toString().contains(componentClassSubstring);

    return getComponentByClass(JOptionPane.getRootFrame(), predicate);
  }

  /**
   * To be removed.
   */
  public static @Nullable Component getButtonByActionClass(@NotNull String actionClassSubstring) {
    Predicate<Component> predicate = (c) -> c instanceof AnActionHolder
        && ((AnActionHolder) c).getAction().getClass().toString().contains(actionClassSubstring);

    return getComponentByClass(JOptionPane.getRootFrame(), predicate);
  }

  private ComponentLocator() {

  }
}
