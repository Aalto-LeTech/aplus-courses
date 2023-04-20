package fi.aalto.cs.apluscourses.model.tutorial.switching;

import org.jetbrains.annotations.NotNull;

public interface Switch<S extends Switch<?>> {
  default void connect(@NotNull S connection) {
    throw new UnsupportedOperationException();
  }
}
