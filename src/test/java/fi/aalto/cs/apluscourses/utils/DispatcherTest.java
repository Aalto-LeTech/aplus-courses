package fi.aalto.cs.apluscourses.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class DispatcherTest {

  @Test
  @SuppressWarnings("unchecked")
  void testDispatcher() {
    Dispatcher.FunctionDelegate<Object, Integer, String> objectToString =
        mock(Dispatcher.FunctionDelegate.class);
    doReturn("object turned to string").when(objectToString).callFor(any(), anyInt());

    Dispatcher.FunctionDelegate<String, Integer, String> stringToString =
        mock(Dispatcher.FunctionDelegate.class);
    doReturn("string turned to string").when(stringToString).callFor(anyString(), anyInt());

    Dispatcher<Integer, String> dispatcher = new Dispatcher<>();

    dispatcher.register(Object.class, objectToString);
    dispatcher.register(String.class, stringToString);

    Object object = new Object();
    dispatcher.call(object, 42);
    verify(objectToString).callFor(object, 42);

    String string = "somestr";
    dispatcher.call(string, 33);
    verify(stringToString).callFor(string, 33);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testNoFunctionFound() {
    Dispatcher.FunctionDelegate<String, Boolean, String> stringToString =
        mock(Dispatcher.FunctionDelegate.class);
    doReturn("string turned to string").when(stringToString).callFor(anyString(), anyBoolean());

    Dispatcher<Boolean, String> dispatcher = new Dispatcher<>();

    dispatcher.register(String.class, stringToString);

    // There is no function delegate for object, so this method call throws
    assertThrows(IllegalArgumentException.class, () ->
        dispatcher.call(new Object(), true));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSuperclassFunctionIsUsedIfNeeded() {
    Dispatcher.FunctionDelegate<Object, Character, String> objectToString =
        mock(Dispatcher.FunctionDelegate.class);
    doReturn("object turned to string").when(objectToString).callFor(any(), anyChar());

    Dispatcher<Character, String> dispatcher = new Dispatcher<>();

    dispatcher.register(Object.class, objectToString);

    String string = "mystr";
    dispatcher.call(string, 'a');
    verify(objectToString).callFor(string, 'a');
  }

}
