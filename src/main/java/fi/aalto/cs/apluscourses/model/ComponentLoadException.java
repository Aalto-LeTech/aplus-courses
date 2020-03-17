package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentLoadException extends Exception {
  @NotNull
  private final Component component;

  public ComponentLoadException(@NotNull Component component, @Nullable Throwable cause) {
    super("Could not load component '" + component.getName() + "'.", cause);
    this.component = component;
  }

  @NotNull
  public Component getComponent() {
    return component;
  }
}
