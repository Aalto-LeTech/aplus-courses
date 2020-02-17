package fi.aalto.cs.apluscourses.model;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface ModuleInstaller {
  void installAsync(@NotNull List<Module> modules);

  void install(@NotNull List<Module> modules);

  void install(Module modules);

  interface Factory {
    ModuleInstaller getInstallerFor(Course course);
  }
}
