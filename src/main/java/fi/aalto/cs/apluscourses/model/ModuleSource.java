package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public interface ModuleSource {
  @NotNull
  Module getModule(String moduleName) throws NoSuchModuleException;
}
