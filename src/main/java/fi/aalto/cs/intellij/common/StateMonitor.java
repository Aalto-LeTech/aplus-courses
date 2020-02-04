package fi.aalto.cs.intellij.common;

import org.jetbrains.annotations.NotNull;

class StateMonitor {
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

  public void waitFor(int expectedState) {
    Exception exception = null;
    boolean changed = false;
    boolean error = false;
    synchronized (stateLock) {
      while (state < expectedState) {
        if (isError()) {
          error = true;
          break;
        }
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
    if (error) {
      throw new StateException(exception);
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

  public boolean isError() {
    synchronized (stateLock) {
      return state <= ERROR;
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

  public static class StateException extends RuntimeException {
    public StateException(Throwable cause) {
      super(cause);
    }
  }
}
