package fi.aalto.cs.apluscourses.utils.observable;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ValidationError {
  @NotNull
  String getDescription();
}
