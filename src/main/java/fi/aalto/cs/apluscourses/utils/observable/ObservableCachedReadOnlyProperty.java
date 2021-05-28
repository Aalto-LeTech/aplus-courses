package fi.aalto.cs.apluscourses.utils.observable;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObservableCachedReadOnlyProperty<T> extends ObservableReadOnlyProperty<T> {

  private T cached;

  private boolean isInitialized = false;

  public ObservableCachedReadOnlyProperty(@NotNull Supplier<T> getter) {
    super(getter);
  }

  @Override
  protected synchronized void onValueChanged(@Nullable T value, @Nullable Object source) {
    isInitialized = false;
    cached = null;
    super.onValueChanged(value, source);
  }

  @Override
  protected synchronized void onValueChanged(@Nullable Object source) {
    isInitialized = false;
    cached = null;
    super.onValueChanged(source);
  }

  @Nullable
  @Override
  public T get() {
    synchronized (this) {
      if (!isInitialized) {
        cached = getter.get();
        isInitialized = true;
      }
      return cached;
    }
  }
}
