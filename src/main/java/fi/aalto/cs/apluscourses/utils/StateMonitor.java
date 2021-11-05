package fi.aalto.cs.apluscourses.utils;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * A synchronized implementation of incrementally changing state.  Different states are represented
 * as different integers, semantics of which can be chosen by the client code with the following
 * rules:
 * <ul>
 *   <li>The initial state is {@code INITIAL}</li>
 *   <li>All the states greater than or equal to {@code INITIAL} are considered non-error states.
 *   All the states less than or equal to {@code ERROR} are considered error states.</li>
 *   <li>Normally, the state can only increase, that is, on a single change, a new state can never
 *   be less than the old state, except when either the old state or the new state is an error
 *   state.</li>
 * </ul>
 *
 * <p>The {@link StateListener}, given in the constructor is notified of the changes in state.</p>
 */
public class StateMonitor {
  private int state;
  private final Object stateLock = new Object();
  private final StateListener listener;

  public static final int ERROR = -1;
  public static final int INITIAL = 0;

  public StateMonitor(@NotNull StateListener stateListener) {
    this(INITIAL, stateListener);
  }

  public StateMonitor(int state, @NotNull StateListener stateListener) {
    this.state = state;
    this.listener = stateListener;
  }

  /**
   * Sets the state to {@code newState}.
   *
   * @param newState New state.
   * @return Whether or not there was a change.
   */
  public boolean set(int newState) {
    if (setInternal(newState)) {
      onChanged(newState);
      return true;
    }
    return false;
  }

  /**
   * If the current state equals any one of {@code expectedStates}, sets it to {@code newState} and
   * returns true.  Otherwise, returns false.
   *
   * @param newState       New state.
   * @param expectedStates Expected states.
   * @return Whether or not the current state in the beginning was equal to any of
   * {@code expectedStates}.
   * @throws IllegalArgumentException If {@code newState} is less than any of {@code expectedStates}
   *                                  (and is not an error state).
   */
  public boolean setConditionallyTo(int newState, int... expectedStates) {
    if (!isError(newState)
        && !Arrays.stream(expectedStates).allMatch(expectedState -> expectedState <= newState)) {
      throw new IllegalArgumentException();
    }
    boolean result = false;
    boolean changed = false;
    synchronized (stateLock) {
      for (int expectedState : expectedStates) {
        result = state == expectedState;
        if (result) {
          changed = setInternal(newState);
          break;
        }
      }
    }
    if (changed) {
      onChanged(newState);
    }
    return result;
  }

  /**
   * Waits until the state is either greater than or equal to the {@code expectedState} or error.
   *
   * @param expectedState A state.
   */
  public void waitUntil(int expectedState) {
    synchronized (stateLock) {
      while (!isError(state) && state < expectedState) {
        try {
          stateLock.wait();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private boolean setInternal(int newState) {
    synchronized (stateLock) {
      if (newState < state && !isError(state) && !isError(newState)) {
        throw new IllegalStateException();
      }
      boolean changed = state != newState;
      state = newState;
      stateLock.notifyAll();
      return changed;
    }
  }

  private void onChanged(int newState) {
    listener.onStateChanged(newState);
  }

  /**
   * Returns a boolean value indicating whether the state is an error state.
   *
   * @return True if error, otherwise false.
   */
  public boolean hasError() {
    synchronized (stateLock) {
      return isError(state);
    }
  }

  public static boolean isError(int someState) {
    return someState <= ERROR;
  }

  /**
   * Returns the current state.
   *
   * @return The current state.
   */
  public int get() {
    synchronized (stateLock) {
      return state;
    }
  }

  @FunctionalInterface
  public interface StateListener {
    void onStateChanged(int newState);
  }
}
