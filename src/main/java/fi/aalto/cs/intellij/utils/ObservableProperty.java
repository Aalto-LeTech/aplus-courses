package fi.aalto.cs.intellij.utils;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

public class ObservableProperty<T> {

  @Nullable
  private volatile T value;

  public ObservableProperty(@Nullable T initialValue) {
    value = initialValue;
  }

  private final Set<ValueObserver<T>> observers =
      Collections.newSetFromMap(new WeakHashMap<>());

  public void addValueChangedObserver(ValueObserver<T> observer) {
    T currentValue = value;
    synchronized (observers) {
      observer.valueChanged(currentValue);
      observers.add(observer);
    }
  }

  public void set(T newValue) {
    value = newValue;
    onValueChanged(newValue);
  }

  private void onValueChanged(@Nullable T value) {
    synchronized (observers) {
      for (ValueObserver<T> observer : observers) {
        observer.valueChanged(value);
      }
    }
  }

  public interface ValueObserver<T> {
    void valueChanged(@Nullable T value);
  }

  @Nullable
  public T get() {
    return value;
  }
}
