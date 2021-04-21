package fi.aalto.cs.apluscourses.ui.ideactivities;

import java.awt.Component;
import java.awt.Container;
import java.util.function.Predicate;
import javax.swing.JOptionPane;

import com.intellij.openapi.actionSystem.AnActionHolder;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentLocator {
  public static @Nullable Component getComponentByClass(@NotNull String componentClassSubstring) {
    return getComponentByClass(JOptionPane.getRootFrame(),
            (obj) -> obj.getClass().toString().contains(componentClassSubstring));
  }

  public static @Nullable Component getButtonByActionClass(@NotNull String actionClassSubstring) {
    return getComponentByClass(JOptionPane.getRootFrame(),
            (obj) -> obj instanceof AnActionHolder && ((AnActionHolder) obj).getAction().getClass().toString().contains(actionClassSubstring));
  }

  private static @Nullable Component getComponentByClass(@NotNull Container parentComponent,
                                                         @NotNull Predicate<Object> validityFunc) {
    for (var component : parentComponent.getComponents()) {
      if (validityFunc.test(component)) {
        return component;
      }

      // if a component is a Container, then it can contain more components and we should scan them
      if (component instanceof Container) {
        var checkResult = getComponentByClass((Container) component, validityFunc);
        if (checkResult != null) {
          return checkResult;
        }
      }
    }

    return null;
  }
}
