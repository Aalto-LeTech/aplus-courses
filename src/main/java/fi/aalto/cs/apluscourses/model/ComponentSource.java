package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ComponentSource {
  /**
   * Returns the module with the given name.
   * @throws NoSuchModuleException If the module source doesn't have a module with the given name.
   */
  @NotNull
  default Module getModule(@NotNull String moduleName) throws NoSuchModuleException {
    Module module = getModuleOpt(moduleName);
    if (module == null) {
      throw new NoSuchModuleException(moduleName, null);
    }
    return module;
  }

  @Nullable
  Module getModuleOpt(@NotNull String componentName);
}
