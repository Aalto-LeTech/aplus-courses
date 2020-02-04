package fi.aalto.cs.intellij.model;

import org.jetbrains.annotations.Nullable;

public interface ModuleSource {
  @Nullable
  Module getModule(String moduleName);
}
