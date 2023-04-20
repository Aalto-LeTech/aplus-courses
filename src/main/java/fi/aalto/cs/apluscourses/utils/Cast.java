package fi.aalto.cs.apluscourses.utils;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Cast<U> implements Function<@Nullable Object, @Nullable U> {
  private final @NotNull Class<U> clazz;

  protected Cast(@NotNull Class<U> clazz) {
    this.clazz = clazz;
  }

  public static <U> @NotNull Cast<U> to(@NotNull Class<U> clazz) {
    return new Cast<>(clazz);
  }

  public @Nullable U orNull(@Nullable Object obj) {
    return clazz.isInstance(obj) ? clazz.cast(obj) : null;
  }

  @Override
  public @Nullable U apply(@Nullable Object obj) throws ClassCastException {
    return clazz.cast(obj);
  }
}
