package fi.aalto.cs.apluscourses.presentation.dialogs;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Dialogs {

  @NotNull
  Dialog create(@NotNull Object object);
}
