package fi.aalto.cs.apluscourses.utils;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public class CollectionUtil {
  private CollectionUtil() {

  }

  public static <T, R> List<R> mapWithIndex(@NotNull List<T> list,
                                            @NotNull BiFunction<T, Integer, R> func,
                                            int startIndex) {
    return IntStream
        .range(0, list.size())
        .mapToObj(i -> func.apply(list.get(i), startIndex + i))
        .collect(Collectors.toList());
  }

  public static <T> T getNth(@NotNull Iterator<T> iterator,
                             @NotNull Predicate<T> predicate,
                             int n) {
    if (!iterator.hasNext() || n < 0) {
      throw new IndexOutOfBoundsException();
    }
    T current = iterator.next();
    if (predicate.test(current)) {
      if (n == 0) {
        return current;
      }
      return getNth(iterator, predicate, n - 1);
    }
    return getNth(iterator, predicate, n);
  }
}
