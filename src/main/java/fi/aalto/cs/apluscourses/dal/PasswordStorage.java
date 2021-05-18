package fi.aalto.cs.apluscourses.dal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PasswordStorage {
  boolean store(@NotNull String user, char @Nullable [] password);

  void remove();

  char @Nullable [] restorePassword();

  interface Factory {
    @Nullable
    PasswordStorage create(@NotNull String service);

  }
}
