package fi.aalto.cs.apluscourses.utils.observable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ObservableReadOnlyProperty<T> extends ObservableProperty<T> {

  @NotNull
  private final Supplier<T> getter;

  public ObservableReadOnlyProperty(@NotNull Supplier<T> getter) {
    this.getter = getter;
  }

  @Nullable
  @Override
  public T get() {
    return getter.get();
  }
}
