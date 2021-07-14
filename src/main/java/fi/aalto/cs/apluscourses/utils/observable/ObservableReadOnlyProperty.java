package fi.aalto.cs.apluscourses.utils.observable;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObservableReadOnlyProperty<T> extends ObservableProperty<T> {

  @NotNull
  protected final Supplier<T> getter;

  public ObservableReadOnlyProperty(@NotNull Supplier<T> getter) {
    this.getter = getter;
  }

  @Nullable
  @Override
  public T get() {
    return getter.get();
  }
}
