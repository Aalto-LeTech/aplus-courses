package fi.aalto.cs.intellij.utils;

import java.util.Map;
import java.util.WeakHashMap;

public class Event {
  private final Map<Object, Callback<?>> callbacks = new WeakHashMap<>();

  public <T> void addListener(T listener, Callback<T> callback) {
    synchronized (callbacks) {
      callbacks.put(listener, callback);
    }
  }

  public void trigger() {
    synchronized (callbacks) {
      for (Object listener : callbacks.keySet()) {
        callbacks.get(listener).callbackUntyped(listener);
      }
    }
  }

  @FunctionalInterface
  public interface Callback<T> {
    void callback(T listener);

    @SuppressWarnings("unchecked")
    default void callbackUntyped(Object listener) {
      callback((T) listener);
    }
  }
}
