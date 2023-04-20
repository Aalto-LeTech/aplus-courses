package fi.aalto.cs.apluscourses.utils;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class FuncUtil {
  private FuncUtil() {

  }

  public static <T, U, R> @NotNull Function<T, R> bindSecond(@NotNull BiFunction<T, U, R> biFunction, U second) {
    return t -> biFunction.apply(t, second);
  }

  public static <T, U> @NotNull Consumer<T> bindSecond(@NotNull BiConsumer<T, U> biConsumer, U second) {
    return t -> biConsumer.accept(t, second);
  }
}
