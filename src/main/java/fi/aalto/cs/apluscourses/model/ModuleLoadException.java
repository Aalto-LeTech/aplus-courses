package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleLoadException extends Exception {
  @NotNull
  private final transient Module module;

  public ModuleLoadException(@NotNull Module module, @Nullable Throwable cause) {
    super("Could not load module '" + module.getName() + "'.", cause);
    this.module = module;
  }

  @NotNull
  public Module getModule() {
    return module;
  }
}
