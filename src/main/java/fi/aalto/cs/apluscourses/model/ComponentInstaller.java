package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ComponentInstaller {
  default void installAsync(@NotNull List<Component> components) {
    installAsync(components, null);
  }

  void installAsync(@NotNull List<Component> components, @Nullable Runnable callback);

  void install(@NotNull List<Component> components);

  void install(Component component);

  interface Factory {
    @NotNull
    ComponentInstaller getInstallerFor(@NotNull ComponentSource componentSource,
                                       @NotNull Dialogs dialogs);
  }

  interface Dialogs {
    boolean shouldOverwrite(@NotNull Component component);
  }
}
