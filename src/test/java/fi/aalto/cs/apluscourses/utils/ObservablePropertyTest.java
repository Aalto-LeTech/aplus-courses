package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

public class ObservablePropertyTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testObservableProperty() {
    Object initialValue = new Object();
    ObservableProperty<Object> property = new ObservableProperty<>(initialValue);
    assertEquals("Value should be initial value.", initialValue, property.get());

    ObservableProperty.ValueObserver<Object> observer1 =
        mock(ObservableProperty.ValueObserver.class);
    property.addValueObserver(observer1);
    verify(observer1).valueChanged(initialValue);

    Object newValue = new Object();
    property.set(newValue);
    verify(observer1).valueChanged(newValue);
    assertEquals("Value should be new value.", newValue, property.get());

    ObservableProperty.ValueObserver<Object> observer2 =
        mock(ObservableProperty.ValueObserver.class);
    property.addValueObserver(observer2);
    verify(observer2).valueChanged(newValue);

    Object finalValue = new Object();
    property.set(finalValue);
    verify(observer1).valueChanged(finalValue);
    verify(observer2).valueChanged(finalValue);
    assertEquals("Value should be final value.", finalValue, property.get());

    verifyNoMoreInteractions(observer1);
    verifyNoMoreInteractions(observer2);
  }
}
