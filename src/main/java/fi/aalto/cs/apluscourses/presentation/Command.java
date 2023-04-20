package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.presentation.base.PresentationContext;
import org.jetbrains.annotations.NotNull;

public interface Command {
  void execute(@NotNull PresentationContext context);

  default boolean canExecute(@NotNull PresentationContext context) {
    return true;
  }
}
