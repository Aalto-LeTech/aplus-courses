package fi.aalto.cs.apluscourses.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EventTest {

  @SuppressWarnings("unchecked")
  @Test
  void testEvent() {
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

    verify(callback1).callbackUntyped(listener1, null);
    verify(callback2).callbackUntyped(listener2, null);

    verifyNoMoreInteractions(callback1);
    verifyNoMoreInteractions(callback2);

    // Prevent listeners from getting GC'ed
    Assertions.assertNotNull(listener1);
    Assertions.assertNotNull(listener2);
  }
}
