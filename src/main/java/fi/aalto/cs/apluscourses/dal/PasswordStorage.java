package fi.aalto.cs.apluscourses.dal;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PasswordStorage {

  void store(@NotNull String user,
             char @Nullable [] password,
             @NotNull Runnable onSuccess,
             @NotNull Runnable onFailure);

  void remove();

  void restorePassword(@NotNull Consumer<char @NotNull []> onSuccess, @NotNull Runnable onFailure);

  interface Factory {
    @Nullable
    PasswordStorage create(@NotNull String service);

  }
}
