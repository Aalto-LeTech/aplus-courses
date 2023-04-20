package fi.aalto.cs.apluscourses.utils;

import java.util.Comparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface EqualityComparator<T> {
  boolean equals(@Nullable T o1, @Nullable T o2);

  static <T> @NotNull EqualityComparator<T> from(@NotNull Comparator<T> comparator) {
    return new ComparatorWrapper<>(comparator);
  }

  class ComparatorWrapper<T> implements EqualityComparator<T> {
    private final @NotNull Comparator<T> comparator;

    public ComparatorWrapper(@NotNull Comparator<T> comparator) {
      this.comparator = comparator;
    }

    @Override
    public boolean equals(@Nullable T o1, @Nullable T o2) {
      return o1 == o2 || o1 != null && o2 != null && comparator.compare(o1, o2) == 0;
    }
  }
}
