package fi.aalto.cs.apluscourses.utils;

import java.util.AbstractList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Lisp-like list implementation.
 * Inspired by https://stackoverflow.com/a/2634421
 *
 * @param <T> Type of list elements.
 */
public class ConsList<T> extends AbstractList<T> {
  @NotNull
  private final T first;
  @NotNull
  private final List<T> rest;

  public ConsList(@NotNull T first, @NotNull List<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  @Override
  public T get(int i) {
    return i == 0 ? first : rest.get(i - 1);
  }

  @Override
  public int size() {
    return 1 + rest.size();
  }
}
