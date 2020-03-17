package fi.aalto.cs.apluscourses.model;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface Installer {
  void installAsync(@NotNull List<Component> components);

  void install(@NotNull List<Component> components);

  void install(Component component);

  interface Factory {
    Installer getInstallerFor(ComponentSource componentSource);
  }
}
