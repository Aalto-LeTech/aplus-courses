package fi.aalto.cs.apluscourses.utils;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FactorySelectorTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testFactorySelector() {
    FactorySelector.Factory<Object, Integer, String> objectToString =
        mock(FactorySelector.Factory.class);
    doReturn("object turned to string").when(objectToString).create(any(), anyInt());

    FactorySelector.Factory<String, Integer, String> stringToString =
        mock(FactorySelector.Factory.class);
    doReturn("string turned to string").when(stringToString).create(anyString(), anyInt());

    FactorySelector<Integer, String> selector = new FactorySelector<>();

    selector.register(Object.class, objectToString);
    selector.register(String.class, stringToString);

    Object object = new Object();
    selector.create(object, 42);
    verify(objectToString).create(object, 42);

    String string = "somestr";
    selector.create(string, 33);
    verify(stringToString).create(string, 33);
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("unchecked")
  public void testNoFactoryFound() {
    FactorySelector.Factory<String, Boolean, String> stringToString =
        mock(FactorySelector.Factory.class);
    doReturn("string turned to string").when(stringToString).create(anyString(), anyBoolean());

    FactorySelector<Boolean, String> selector = new FactorySelector<>();

    selector.register(String.class, stringToString);

    // There is no factory for object, so this method call throws
    selector.create(new Object(), true);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSuperclassFactoryIsUsedIfNeeded() {
    FactorySelector.Factory<Object, Character, String> objectToString =
        mock(FactorySelector.Factory.class);
    doReturn("object turned to string").when(objectToString).create(any(), anyChar());

    FactorySelector<Character, String> selector = new FactorySelector<>();

    selector.register(Object.class, objectToString);

    String string = "mystr";
    selector.create(string, 'a');
    verify(objectToString).create(string, 'a');
  }

}
