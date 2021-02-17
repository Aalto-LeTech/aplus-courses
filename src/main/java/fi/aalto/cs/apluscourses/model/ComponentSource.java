package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ComponentSource {
  /**
   * Returns the component with the given name.
   * @throws NoSuchComponentException If the source doesn't have a component with the given name.
   */
  @NotNull
  default Component getComponent(@NotNull String componentName) throws NoSuchComponentException {
    return Optional.ofNullable(getComponentIfExists(componentName))
       .orElseThrow(() -> new NoSuchComponentException(componentName, null));
  }

  /**
   * Returns the component with the given name if it exists, otherwise null.
   */
  @Nullable
  Component getComponentIfExists(@NotNull String componentName);

  /**
   * Maps a list of component names to a list of components.
   *
   * @param componentNames A list of names.
   * @return A list of {@link Component} objects.
   * @throws NoSuchComponentException If a component is not found.
   */
  @NotNull
  default List<Component> getComponents(@NotNull List<String> componentNames)
      throws NoSuchComponentException {
    List<Component> components = new ArrayList<>(componentNames.size());
    for (String componentName : componentNames) {
      components.add(getComponent(componentName));
    }
    return components;
  }
}
