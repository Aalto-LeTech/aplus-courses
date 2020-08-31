package fi.aalto.cs.apluscourses.utils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public class ListUtil {
  private ListUtil() {

  }

  public static <T, R> List<R> mapWithIndex(@NotNull List<T> list,
                                            @NotNull BiFunction<T, Integer, R> func,
                                            int startIndex) {
    return IntStream
        .range(startIndex, startIndex + list.size())
        .mapToObj(i -> func.apply(list.get(i), i))
        .collect(Collectors.toList());
  }
}
