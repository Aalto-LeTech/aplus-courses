package fi.aalto.cs.intellij.utils;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

public class ObservableProperty<T> {

  @Nullable
  private volatile T value;

  public ObservableProperty(T initialValue) {
    value = initialValue;
  }

  private final Set<ValueChangedObserver<T>> observers =
      Collections.newSetFromMap(new WeakHashMap<>());

  public void addValueChangedObserver(ValueChangedObserver<T> observer) {
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
      for (ValueChangedObserver<T> observer : observers) {
        observer.valueChanged(value);
      }
    }
  }

  public interface ValueChangedObserver<T> {
    void valueChanged(@Nullable T value);
  }

  @Nullable
  public T get() {
    return value;
  }
}
