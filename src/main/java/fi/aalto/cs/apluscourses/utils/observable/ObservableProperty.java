package fi.aalto.cs.apluscourses.utils.observable;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A property that notifies its registered observers of the changes to its value.
 * @param <T> Type of the property's value.
 */
public abstract class ObservableProperty<T> {
  private final Map<Object, Callback<?, T>> observers = new WeakHashMap<>();
  @NotNull
  private final Validator<T> validator;

  public ObservableProperty() {
    this(null);
  }

  public ObservableProperty(@Nullable Validator<T> validator) {
    this.validator = Optional.ofNullable(validator).orElse(whatever -> null);
  }

  /**
   * Add a new observer and calls its {@code valueChanged} method.
   *
   * <p>The observers are only weakly referenced.  If this class is used to notify the UI of the
   * changes in the model, it is a good idea to make the visible UI component to have a strong
   * reference to the observer object.  That way, it is guaranteed that the observer is not GC'ed
   * before the UI component.</p>
   *
   * <p>After the given observer has been added to the set of observers, {@code valueChanged} of
   * its callback is immediately called with the current value of the property.  Therefore, there
   * should not be any reason to call {@code get()} method when using an observer.  In other words,
   * {@code addValueObserver()} and {@code get()} are two separate interfaces to the
   * property, only one of which should be used at once.</p>
   *
   * <p>If observer already was in the set of the observers, this method replaces it's callback.</p>
   *
   * @param observer An observer.
   * @param callback A callback whose method {@code valueChanged} should never call
   *                 {@code addValueObserver()}.
   */
  public synchronized <O> void addValueObserver(@NotNull O observer,
                                                @NotNull Callback<O, T> callback) {
    observers.put(observer, callback);
    callback.valueChanged(observer, get());
  }

  public <O> void addValueObserver(@NotNull O observer, @NotNull SimpleCallback<O> callback) {
    addValueObserver(observer, new CallbackWrapper<>(callback));
  }

  public void declareDependentOn(@NotNull ObservableProperty<?> property) {
    property.addValueObserver(this, (self, dummy) -> self.onValueChanged(self.get()));
  }

  @Nullable
  public abstract T get();

  public void set(T value) {
    throw new UnsupportedOperationException();
  }

  protected synchronized void onValueChanged(@Nullable T value) {
    for (Map.Entry<Object, Callback<?, T>> entry : observers.entrySet()) {
      entry.getValue().valueChangedUntyped(entry.getKey(), value);
    }
  }

  public synchronized void removeValueObserver(Object observer) {
    observers.remove(observer);
  }

  public ValidationError validate() {
    return validator.validate(get());
  }

  public interface Callback<O, T> {
    void valueChanged(@NotNull O observer, @Nullable T value);

    @SuppressWarnings("unchecked")
    default void valueChangedUntyped(@NotNull Object observer, @Nullable T value) {
      valueChanged((O) observer, value);
    }
  }

  public interface SimpleCallback<O> {
    void valueChanged(@NotNull O observer);
  }

  private static class CallbackWrapper<O, T> implements Callback<O, T> {

    @NotNull
    private final SimpleCallback<O> callback;

    public CallbackWrapper(@NotNull SimpleCallback<O> callback) {
      this.callback = callback;
    }

    @Override
    public void valueChanged(@NotNull O observer, @Nullable T value) {
      callback.valueChanged(observer);
    }
  }

  @FunctionalInterface
  public interface Validator<T> {
    @Nullable
    ValidationError validate(@Nullable T value);
  }
}
