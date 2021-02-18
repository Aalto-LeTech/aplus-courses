package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentLoadException extends Exception {

  private static final long serialVersionUID = -3674405035175333225L;
  @NotNull
  private final String componentName;

  public ComponentLoadException(@NotNull String componentName, @Nullable Throwable cause) {
    super("Could not load component '" + componentName + "'.", cause);
    this.componentName = componentName;
  }

  @NotNull
  public String getComponentName() {
    return componentName;
  }
}
