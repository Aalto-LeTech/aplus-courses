package fi.aalto.cs.apluscourses.utils.observable;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObservableDelegateProperty<T> extends ObservableProperty<T> {

  @NotNull
  private final Supplier<T> getter;
  @NotNull
  private final Consumer<T> setter;

  public ObservableDelegateProperty(@NotNull Supplier<T> getter, @NotNull Consumer<T> setter) {
    this.getter = getter;
    this.setter = setter;
  }

  @Nullable
  @Override
  public T get() {
    return getter.get();
  }

  @Override
  public void set(@Nullable T value) {
    setter.accept(value);
  }
}
