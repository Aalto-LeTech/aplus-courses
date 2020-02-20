package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class StateMonitorTest {

  @Test
  public void testSetAndGet() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    assertEquals("get() should return INITIAL state", StateMonitor.INITIAL, stateMonitor.get());

    verifyNoInteractions(stateListener);

    int newState = 42;
    stateMonitor.set(newState);

    verify(stateListener).onStateChanged();

    assertEquals("get() should return new state", newState, stateMonitor.get());

    verifyNoMoreInteractions(stateListener);
  }

  @Test
  public void testSetConditionallyReturnsTrue() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    int expectedState = 15;
    stateMonitor.set(expectedState);

    int newState = 1337;
    assertTrue("setConditionally() should return true",
        stateMonitor.setConditionally(expectedState, newState));

    verify(stateListener, times(2)).onStateChanged();

    assertEquals("get() should return new state", newState, stateMonitor.get());


    verifyNoMoreInteractions(stateListener);
  }

  @Test
  public void testSetConditionallyReturnsFalse() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    int expectedState = 20;
    int newState = 220;
    assertFalse("setConditionally() should return false",
        stateMonitor.setConditionally(expectedState, newState));

    assertEquals("get() should return the initial state", StateMonitor.INITIAL, stateMonitor.get());

    verifyNoInteractions(stateListener);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetConditionallyThrowsException() {
    StateMonitor.StateListener stateListener = mock(StateMonitor.StateListener.class);
    StateMonitor stateMonitor = new StateMonitor(stateListener);

    stateMonitor.setConditionally(100, 20);

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
