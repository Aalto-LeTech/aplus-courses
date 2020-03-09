package fi.aalto.cs.apluscourses.model;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public interface ModuleSource {
  @NotNull
  default Module getModule(@NotNull String moduleName) throws NoSuchModuleException {
    return getModuleOptional(moduleName)
        .orElseThrow(() -> new NoSuchModuleException(moduleName, null));
  }

  @NotNull
  Optional<Module> getModuleOptional(@NotNull String moduleName);
}
