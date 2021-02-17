package fi.aalto.cs.apluscourses.utils;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StateMonitorTest {

  @Test
  public void testSetAndGet() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    assertEquals("get() should return INITIAL state", StateMonitor.INITIAL, stateMonitor.get());

    verifyNoInteractions(stateListener);

    int newState = 42;
    stateMonitor.set(newState);

    verify(stateListener).onStateChanged(newState);

    assertEquals("get() should return new state", newState, stateMonitor.get());

    verifyNoMoreInteractions(stateListener);
  }

  @Test
  public void testSetConditionallyToReturnsTrue() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    int expectedState = 15;
    stateMonitor.set(expectedState);
    verify(stateListener).onStateChanged(expectedState);

    int newState = 1337;
    assertTrue("setConditionally() should return true",
        stateMonitor.setConditionallyTo(newState, expectedState));
    verify(stateListener).onStateChanged(newState);

    assertEquals("get() should return new state", newState, stateMonitor.get());

    verifyNoMoreInteractions(stateListener);
  }

  @Test
  public void testSetConditionallyToReturnsFalse() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    int expectedState = 20;
    int newState = 220;
    assertFalse("setConditionally() should return false",
        stateMonitor.setConditionallyTo(newState, expectedState));

    assertEquals("get() should return the initial state", StateMonitor.INITIAL, stateMonitor.get());

    verifyNoInteractions(stateListener);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetConditionallyToThrowsException() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    stateMonitor.setConditionallyTo(20, 100);

    verifyNoInteractions(stateListener);
  }

  @Test
  public void testWaitUntil() throws InterruptedException {
    StateMonitor stateMonitor = new StateMonitor(mock(StateMonitor.StateListener.class));

    int expectedState = 400;
    AtomicBoolean waitOver = new AtomicBoolean(false);

    Thread thread = new Thread(() -> {
      stateMonitor.waitUntil(expectedState);
      waitOver.set(true);

    });
    thread.start();

    Thread.sleep(20); // Let's go to sleep and give the other thread a possibility to proceed...

    assertFalse("The other thread should still be waiting", waitOver.get());

    stateMonitor.set(expectedState);
    thread.join();

    assertTrue("The other thread should have been run", waitOver.get());
  }

  @Test
  public void testWaitUntilInterruption() throws InterruptedException {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);
    Thread thread = new Thread(() -> stateMonitor.waitUntil(9999));

    Thread.sleep(20);

    thread.interrupt();
    thread.join();

    verifyNoInteractions(stateListener);
  }
}
