package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.async.Awaitable;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ComponentInstaller {
  default Awaitable installAsync(@NotNull List<Component> components) {
    return installAsync(components, null);
  }

  Awaitable installAsync(@NotNull List<Component> components, @Nullable Runnable callback);

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
