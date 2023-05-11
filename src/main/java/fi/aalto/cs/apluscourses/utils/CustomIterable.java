package fi.aalto.cs.apluscourses.utils;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomIterable<T> implements Iterable<@NotNull T> {

  private final @Nullable T root;
  private final @NotNull Function<@NotNull T, @Nullable T> proceed;

  private CustomIterable(@Nullable T root, @NotNull Function<@NotNull T, @Nullable T> proceed) {
    this.root = root;
    this.proceed = proceed;
  }

  /**
   * Behaves similarly to Stream.iterate() except for the iteration ends when the given function returns null.
   */
  public static <T> @NotNull Iterable<@NotNull T> from(@Nullable T root,
                                                       @NotNull Function<@NotNull T, @Nullable T> proceed) {
    return new CustomIterable<>(root, proceed);
  }

  @Override
  public @NotNull Iterator<@NotNull T> iterator() {
    return new MyIterator();
  }

  private class MyIterator implements Iterator<@NotNull T> {
    private @Nullable T next = root;

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public T next() {
      var current = Objects.requireNonNull(next);
      next = proceed.apply(current);
      return current;
    }
  }
}
