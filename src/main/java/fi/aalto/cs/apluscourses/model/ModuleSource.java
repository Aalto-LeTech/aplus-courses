package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.Nullable;

public interface ModuleSource {
  @Nullable
  Module getModule(String moduleName);
}
