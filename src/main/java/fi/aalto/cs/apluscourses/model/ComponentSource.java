package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public interface ComponentSource {
  @NotNull
  Module getComponent(String componentName) throws NoSuchModuleException;
}
