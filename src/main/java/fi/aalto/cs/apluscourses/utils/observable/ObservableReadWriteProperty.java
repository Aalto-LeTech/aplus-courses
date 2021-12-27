package fi.aalto.cs.apluscourses.utils.observable;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class ObservableReadWriteProperty<T> extends ObservableProperty<T> {

  @Nullable
  private T value;

  /**
   * Construct a new observable property.
   *
   * @param initialValue Initial value of this {@link ObservableReadWriteProperty}
   */
  public ObservableReadWriteProperty(@Nullable T initialValue) {
    this(initialValue, null);
  }

  public ObservableReadWriteProperty(@Nullable T initialValue, @Nullable Validator<T> validator) {
    super(validator);
    value = initialValue;
  }

  /**
   * Sets a new value to the property.
   *
   * @param newValue The new value to be set.
   * @return True, if new value was different than the old value, otherwise false.
   */
  protected synchronized boolean setInternal(T newValue) {
    boolean changed = !Objects.equals(value, newValue);
    value = newValue;
    return changed;
  }

  /**
   * Sets a new value to the property and notifies the observers (that are still alive) by calling
   * {@code valueChanged} of their callbacks (synchronously in an arbitrary order) with the new
   * value, unless the new value equals the old value, in which case observers are not notified.
   *
   * @param newValue The new value to be set.
   */
  @Override
  public synchronized void set(T newValue) {
    set(newValue, null);
  }

  @Override
  public synchronized void set(T newValue, @Nullable Object source) {
    if (setInternal(newValue)) {
      onValueChanged(newValue, source);
    }
  }

  @Nullable
  @Override
  public synchronized T get() {
    return value;
  }

  @Nullable
  @Override
  public synchronized T getAndSet(T value) {
    T oldValue = get();
    set(value);
    return oldValue;
  }
}
