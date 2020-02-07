package fi.aalto.cs.apluscourses.utils;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A property that notifies its registered observers of the changes to its value.
 * @param <T> Type of the property's value.
 */
public class ObservableProperty<T> {

  @Nullable
  private volatile T value;

  public ObservableProperty(@Nullable T initialValue) {
    value = initialValue;
  }

  private final Set<ValueObserver<T>> observers =
      Collections.newSetFromMap(new WeakHashMap<>());

  /**
   * Add a new {@link ValueObserver} and calls its {@code valueChanged} method.
   *
   * <p>The observers are only weakly referenced.  If this class is used to notify the UI of the
   * changes in the model, it is a good idea to make the visible UI component to have a strong
   * reference to the observer object.  That way, it is guaranteed that the observer is not GC'ed
   * before the UI component.</p>
   *
   * <p>After the given observer has been added to the set of observers, {@code valueChanged} of
   * that observer is immediately called with the current value of the property.  Therefore, there
   * should not be any reason to call {@code get()} method when using an observer.  In other words,
   * {@code addValueChangedObserver()} and {@code get()} are two separate interfaces to the
   * property, only one of which should be used at once.</p>
   *
   * <p>If observer already was in the set of the observers, this method does nothing.</p>
   *
   * @param observer A {@link ValueObserver}.  Observer's method {@code valueChanged} should never
   *                 call {@code addValueChangedObserver()}.
   */
  public void addValueChangedObserver(@NotNull ValueObserver<T> observer) {
    synchronized (observers) {
      if (observers.add(observer)) {
        observer.valueChanged(value);
      }
    }
  }

  /**
   * Sets a new value to the property and notifies the observers (that are still alive) by calling
   * their {@code valueChanged} method (synchronously in an arbitrary order) with the new value.
   *
   * @param newValue The new value to be set.
   */
  public void set(T newValue) {
    synchronized (observers) {
      value = newValue;
      onValueChanged(newValue);
    }
  }

  private void onValueChanged(@Nullable T value) {
    for (ValueObserver<T> observer : observers) {
      observer.valueChanged(value);
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
