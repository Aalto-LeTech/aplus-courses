package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public interface ComponentSource {
  /**
   * Returns the component with the given name.
   * @throws NoSuchModuleException If the source doesn't have a component with the given name.
   */
  @NotNull
  Component getComponent(@NotNull String componentName) throws NoSuchModuleException;
}
