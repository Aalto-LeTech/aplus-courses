package fi.aalto.cs.intellij.utils;

import org.jetbrains.annotations.NotNull;

public class StateMonitor {
  private int state = INITIAL;
  private final Object stateLock = new Object();
  private final StateListener listener;

  public static final int ERROR = -1;
  public static final int INITIAL = 0;

  public StateMonitor(@NotNull StateListener stateListener) {
    this.listener = stateListener;
  }

  public void set(int newState) {
    if (setInternal(newState)) {
      onChanged();
    }
  }

  public boolean setConditionally(int expectedState, int newState) {
    boolean result;
    boolean changed = false;
    synchronized (stateLock) {
      result = state == expectedState;
      if (result) {
        changed = setInternal(newState);
      }
    }
    if (changed) {
      onChanged();
    }
    return result;
  }

  public void waitFor(int expectedState) throws InterruptedException {
    InterruptedException exception = null;
    boolean changed = false;
    synchronized (stateLock) {
      while (ERROR < state && state < expectedState) {
        try {
          stateLock.wait();
        } catch (InterruptedException e) {
          exception = e;
          changed = setInternal(ERROR);
        }
      }
    }
    if (changed) {
      onChanged();
    }
    if (exception != null) {
      throw exception;
    }
  }

  private boolean setInternal(int newState) {
    synchronized (stateLock) {
      boolean changed = state != newState;
      state = newState;
      stateLock.notifyAll();
      return changed;
    }
  }

  private void onChanged() {
    listener.onStateChanged();
  }

  public int get() {
    synchronized (stateLock) {
      return state;
    }
  }

  @FunctionalInterface
  public interface StateListener {
    void onStateChanged();
  }

  public static class ErrorStateException extends Exception {

  }
}
