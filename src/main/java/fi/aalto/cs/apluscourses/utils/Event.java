package fi.aalto.cs.apluscourses.utils;

import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Event extends EventWithArg<Void> {
  public <T> void addListener(@NotNull T listener,
                              @NotNull Callback<T> callback,
                              @Nullable Executor executor) {
    addListenerWithArg(listener, callback, executor);
  }

  public <T> void addListener(@NotNull T listener, @NotNull Callback<T> callback) {
    addListenerWithArg(listener, callback, null);
  }

  public void trigger() {
    trigger(null);
  }

  @FunctionalInterface
  public interface Callback<T> extends EventWithArg.Callback<T, Void> {
    void callback(@NotNull T listener);

    @Override
    default void callback(@NotNull T listener, Void arg) {
      callback(listener);
    }
  }
}
