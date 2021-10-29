package fi.aalto.cs.apluscourses.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;

public class EventTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testEvent() {
    Object listener1 = new Object();
    Object listener2 = new Object();
    Event.Callback<Object> callback1 = mock(Event.Callback.class);
    Event.Callback<Object> callback2 = mock(Event.Callback.class);

    Event event = new Event();
    event.addListener(listener1, callback1);
    event.addListener(listener2, callback2);

    verifyNoInteractions(callback1);
    verifyNoInteractions(callback2);

    event.trigger();

    verify(callback1).callbackUntyped(listener1);
    verify(callback2).callbackUntyped(listener2);

    verifyNoMoreInteractions(callback1);
    verifyNoMoreInteractions(callback2);

    // Prevent listeners from getting GC'ed
    assertNotNull(listener1);
    assertNotNull(listener2);
  }
}
