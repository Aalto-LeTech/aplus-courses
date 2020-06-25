package fi.aalto.cs.apluscourses.utils.observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

public class ObservablePropertyTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testObservableProperty() {
    Object initialValue = new Object();
    ObservableReadWriteProperty<Object> property = new ObservableReadWriteProperty<>(initialValue);
    assertEquals("Value should be initial value.", initialValue, property.get());

    Object observer1 = new Object();
    ObservableProperty.Callback<Object, Object> callback1 = mock(ObservableProperty.Callback.class);
    property.addValueObserver(observer1, callback1);
    verify(callback1).valueChanged(observer1, initialValue);

    Object newValue = new Object();
    property.set(newValue);
    verify(callback1).valueChangedUntyped(observer1, newValue);
    assertEquals("Value should be new value.", newValue, property.get());

    Object observer2 = new Object();
    ObservableProperty.Callback<Object, Object> callback2 = mock(ObservableProperty.Callback.class);
    property.addValueObserver(observer2, callback2);
    verify(callback2).valueChanged(observer2, newValue);

    Object finalValue = new Object();
    property.set(finalValue);
    verify(callback1).valueChangedUntyped(observer1, finalValue);
    verify(callback2).valueChangedUntyped(observer2, finalValue);
    assertEquals("Value should be final value.", finalValue, property.get());

    verifyNoMoreInteractions(callback1);
    verifyNoMoreInteractions(callback2);

    assertNotNull(observer1);
    assertNotNull(observer2);
  }
}
