package fi.aalto.cs.apluscourses.utils.observable;

import org.jetbrains.annotations.NotNull;

public interface ValidationError {
  @NotNull
  String getDescription();
}
