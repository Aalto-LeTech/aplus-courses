package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Callbacks;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                                       @NotNull Dialogs dialogs,
                                       @NotNull Callbacks callbacks);
  }

  interface Dialogs {
    boolean shouldOverwrite(@NotNull Component component);
  }
}
