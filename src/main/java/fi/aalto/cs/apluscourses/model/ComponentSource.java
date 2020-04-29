package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ComponentSource {
  /**
   * Returns the component with the given name.
   * @throws NoSuchComponentException If the source doesn't have a component with the given name.
   */
  @NotNull
  Component getComponent(@NotNull String componentName) throws NoSuchComponentException;

  /**
   * Returns the component with the given name if it exists, otherwise null.
   */
  @Nullable
  Component getComponentIfExists(@NotNull String componentName);
}
