package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Streamable<T> extends Iterable<T> {
  @NotNull
  default Stream<T> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}
