package fi.aalto.cs.apluscourses.utils.observable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObservablePropertyTest {

  @SuppressWarnings("unchecked")
  @Test
  void testObservableProperty() {
    Object initialValue = new Object();
    ObservableReadWriteProperty<Object> property = new ObservableReadWriteProperty<>(initialValue);
    Assertions.assertEquals(initialValue, property.get(), "Value should be initial value.");

    Object observer1 = new Object();
    ObservableProperty.Callback<Object, Object> callback1 = mock(ObservableProperty.Callback.class);
    property.addValueObserver(observer1, callback1);
    verify(callback1).valueChanged(observer1, initialValue);

    Object newValue = new Object();
    property.set(newValue);
    verify(callback1).valueChangedUntyped(observer1, newValue);
    Assertions.assertEquals(newValue, property.get(), "Value should be new value.");

    Object observer2 = new Object();
    ObservableProperty.Callback<Object, Object> callback2 = mock(ObservableProperty.Callback.class);
    property.addValueObserver(observer2, callback2);
    verify(callback2).valueChanged(observer2, newValue);

    Object finalValue = new Object();
    property.set(finalValue);
    verify(callback1).valueChangedUntyped(observer1, finalValue);
    verify(callback2).valueChangedUntyped(observer2, finalValue);
    Assertions.assertEquals(finalValue, property.get(), "Value should be final value.");

    verifyNoMoreInteractions(callback1);
    verifyNoMoreInteractions(callback2);

    Assertions.assertNotNull(observer1);
    Assertions.assertNotNull(observer2);
  }
}
