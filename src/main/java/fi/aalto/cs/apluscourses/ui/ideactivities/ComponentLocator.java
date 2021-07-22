package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.util.concurrency.annotations.RequiresEdt;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentLocator {
  public static boolean isComponentOfClass(@NotNull Component c, @NotNull String classSubstring) {
    return c.getClass().toString().contains(classSubstring);
  }

  public static boolean hasChildOfClass(@NotNull Container c, @NotNull String classSubstring) {
    return !getComponents(c, comp -> isComponentOfClass(comp, classSubstring)).isEmpty();
  }

  @RequiresEdt
  private static List<Component> getComponents(@NotNull Container parentComponent,
                                               @NotNull Predicate<Component> predicate) {
    List<Component> components = new ArrayList<>();

    for (var component : parentComponent.getComponents()) {
      if (predicate.test(component)) {
        components.add(component);
      }

      // if a component is a Container, then it can contain more components and we should scan them
      if (component instanceof Container) {
        var checkResult = getComponents((Container) component, predicate);
        components.addAll(checkResult);
      }
    }

    return components;
  }

  /**
   * Scans through all components and locates the ones which class name matches a substring.
   * @param classSubstring A case-sensitive substring of the component's desired class.
   */
  @RequiresEdt
  public static List<Component> getComponentsByClass(@NotNull String classSubstring) {
    return getComponents(JOptionPane.getRootFrame(), c -> isComponentOfClass(c, classSubstring));
  }

  @RequiresEdt
  public static @Nullable Component getComponentByClass(@NotNull String componentClassSubstring) {
    return getComponentsByClass(componentClassSubstring).stream().findFirst().orElse(null);
  }

  private ComponentLocator() {

  }
}
