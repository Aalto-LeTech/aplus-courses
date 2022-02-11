package fi.aalto.cs.apluscourses.utils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Lazy<T> {
  private final AtomicReference<@Nullable T> reference = new AtomicReference<>(null);
  private final Supplier<@NotNull T> initializer;

  private Lazy(@NotNull Supplier<@NotNull T> initializer) {
    this.initializer = initializer;
  }

  public static <T> Lazy<T> of(@NotNull Supplier<@NotNull T> initializer) {
    return new Lazy<>(initializer);
  }

  public @NotNull T get() {
    T val = reference.get();
    if (val != null) {
      return val;
    }
    T ourVal = initializer.get();
    T theirVal = reference.compareAndExchange(null, ourVal);
    return theirVal == null ? ourVal : theirVal;
  }
}
