package fi.aalto.cs.apluscourses.model;

import java.util.ArrayList;
import java.util.List;
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
