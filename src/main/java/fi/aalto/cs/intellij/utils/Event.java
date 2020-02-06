package fi.aalto.cs.intellij.utils;

import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.NotNull;

public class Event {
  private final Map<Object, Callback<?>> callbacks = new WeakHashMap<>();

  /**
   * Adds a listener object of type {@link T} for this event.  The listener is weakly referenced by
   * the {@link Event}.  To enable the weak reference, the callback should not strongly refer to the
   * listener (the references to the callbacks are strong).  Static callbacks are recommended.
   *
   * <p>Please note that a single listener object cannot have multiple callbacks.  If this method is
   * called twice with the same listener object, the latter callback replaces the former one.</p>
   *
   * @param listener An object of type {@link T}.
   * @param callback A {@link Callback} which is called when the event is triggered.  The listener
   *                 is given as an argument to the callback.  Note that callbacks should never call
   *                 {@code addListener()}.
   * @param <T> A type of the listener.  For the best reliability, should be one whose
   *            {@code equals} function follows the default paradigm, that is, checks the reference
   *            identity.
   */
  public <T> void addListener(@NotNull T listener, @NotNull Callback<T> callback) {
    synchronized (callbacks) {
      callbacks.put(listener, callback);
    }
  }

  /**
   * Triggers the event, that is, notifies all the registered listeners (who are still alive) by
   * calling their associated callbacks.  Note that the order in which listeners are visited, is
   * arbitrary.  All the callbacks are called synchronously and in the same thread in which this
   * method is called.
   */
  public void trigger() {
    synchronized (callbacks) {
      for (Object listener : callbacks.keySet()) {
        callbacks.get(listener).callbackUntyped(listener);
      }
    }
  }

  @FunctionalInterface
  public interface Callback<T> {
    void callback(@NotNull T listener);

    @SuppressWarnings("unchecked")
    default void callbackUntyped(@NotNull Object listener) {
      callback((T) listener);
    }
  }
}
