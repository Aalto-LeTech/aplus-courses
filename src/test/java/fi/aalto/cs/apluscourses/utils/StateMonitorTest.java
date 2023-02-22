package fi.aalto.cs.apluscourses.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StateMonitorTest {

  @Test
  void testSetAndGet() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    Assertions.assertEquals(StateMonitor.INITIAL, stateMonitor.get(), "get() should return INITIAL state");

    verifyNoInteractions(stateListener);

    int newState = 42;
    stateMonitor.set(newState);

    verify(stateListener).onStateChanged(newState);

    Assertions.assertEquals(newState, stateMonitor.get(), "get() should return new state");

    verifyNoMoreInteractions(stateListener);
  }

  @Test
  void testSetConditionallyToReturnsTrue() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    int expectedState = 15;
    stateMonitor.set(expectedState);
    verify(stateListener).onStateChanged(expectedState);

    int newState = 1337;
    Assertions.assertTrue(stateMonitor.setConditionallyTo(newState, expectedState),
        "setConditionally() should return true");
    verify(stateListener).onStateChanged(newState);

    Assertions.assertEquals(newState, stateMonitor.get(), "get() should return new state");

    verifyNoMoreInteractions(stateListener);
  }

  @Test
  void testSetConditionallyToReturnsFalse() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    int expectedState = 20;
    int newState = 220;
    Assertions.assertFalse(stateMonitor.setConditionallyTo(newState, expectedState),
        "setConditionally() should return false");

    Assertions.assertEquals(StateMonitor.INITIAL, stateMonitor.get(), "get() should return the initial state");

    verifyNoInteractions(stateListener);
  }

  @Test
  void testSetConditionallyToThrowsException() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    assertThrows(IllegalArgumentException.class, () ->
        stateMonitor.setConditionallyTo(20, 100));

    verifyNoInteractions(stateListener);
  }

  @Test
  void testWaitUntil() throws InterruptedException {
    StateMonitor stateMonitor = new StateMonitor(mock(StateMonitor.StateListener.class));

    int expectedState = 400;
    AtomicBoolean waitOver = new AtomicBoolean(false);

    Thread thread = new Thread(() -> {
      stateMonitor.waitUntil(expectedState);
      waitOver.set(true);

    });
    thread.start();

    // Let's go to sleep and give the other thread a possibility to proceed...
    Thread.sleep(20); // NOSONAR

    Assertions.assertFalse(waitOver.get(), "The other thread should still be waiting");

    stateMonitor.set(expectedState);
    thread.join();

    Assertions.assertTrue(waitOver.get(), "The other thread should have been run");
  }

  @Test
  void testWaitUntilInterruption() throws InterruptedException {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);
    Thread thread = new Thread(() -> stateMonitor.waitUntil(9999));

    Thread.sleep(20); //  NOSONAR

    thread.interrupt();
    thread.join();

    verifyNoInteractions(stateListener);
  }
}
