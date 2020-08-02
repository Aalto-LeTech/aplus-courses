package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.Authentication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PasswordStorage {
  boolean store(@NotNull String user, @Nullable char[] password);

  @Nullable
  char[] restorePassword();

  interface Factory {
    @Nullable
    PasswordStorage create(@NotNull String service);

  }
}
