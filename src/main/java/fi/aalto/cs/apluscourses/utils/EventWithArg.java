package fi.aalto.cs.apluscourses.utils;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EventWithArg<A> {
  private final Map<Object, Callback<?, A>> callbacks = new WeakHashMap<>();

  /**
   * Adds a listener object of type {@link T} for this event.  The listener is weakly referenced by
   * the {@link EventWithArg}.  To enable the weak reference, the callback should not strongly refer to the
   * listener (the references to the callbacks are strong).  Static callbacks are recommended.
   *
   * <p>Please note that a single listener object cannot have multiple callbacks.  If this method is
   * called twice with the same listener object, the latter callback replaces the former one.</p>
   *
   * @param listener An object of type {@link T}.
   * @param callback A {@link Callback} which is called when the event is triggered.  The listener
   *                 is given as an argument to the callback.  Note that callbacks should never call
   *                 methods of this event.
   * @param executor An executor for callback.  If null, callback is executed in the same thread
   *                 where {@code trigger} is called.
   * @param <T>      A type of the listener.  For the best reliability, should be one whose
   *                 {@code equals} function follows the default paradigm, that is, checks the reference
   *                 identity.
   */
  public <T> void addListener(@NotNull T listener,
                              @NotNull Callback<T, A> callback,
                              @Nullable Executor executor) {
    synchronized (callbacks) {
      callbacks.put(listener,
              executor == null ? callback : new EventWithArg.CustomExecutorCallbackWrapper<>(executor, callback));
    }
  }

  public <T> void addListener(@NotNull T listener, @NotNull Callback<T, A> callback) {
    addListener(listener, callback, null);
  }

  /**
   * Removes an object from the listener.  If the listener is not there, does nothing.
   *
   * @param listener A listener object to be removed.
   */
  public void removeCallback(@NotNull Object listener) {
    synchronized (callbacks) {
      callbacks.remove(listener);
    }
  }

  /**
   * Triggers the event, that is, notifies all the registered listeners (who are still alive) by
   * calling their associated callbacks.  Note that the order in which listeners are visited, is
   * arbitrary.  All the callbacks are called synchronously and in the same thread in which this
   * method is called.
   */
  public void trigger(A arg) {
    synchronized (callbacks) {
      for (Map.Entry<Object, Callback<?, A>> entry : callbacks.entrySet()) {
        entry.getValue().callbackUntyped(entry.getKey(), arg);
      }
    }
  }

  @FunctionalInterface
  public interface Callback<T, A> {
    void callback(@NotNull T listener, A arg);

    @SuppressWarnings("unchecked")
    default void callbackUntyped(@NotNull Object listener, A arg) {
      callback((T) listener, arg);
    }
  }

  private static class CustomExecutorCallbackWrapper<T, A> implements Callback<T, A> {
    @NotNull
    private final Executor executor;
    @NotNull
    private final Callback<T, A> callback;

    public CustomExecutorCallbackWrapper(@NotNull Executor executor,
                                         @NotNull Callback<T, A> callback) {
      this.executor = executor;
      this.callback = callback;
    }

    @Override
    public void callback(@NotNull T listener, A arg) {
      executor.execute(() -> callback.callback(listener, arg));
    }
  }
}
