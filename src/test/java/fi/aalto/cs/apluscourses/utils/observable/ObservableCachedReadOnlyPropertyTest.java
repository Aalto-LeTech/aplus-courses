package fi.aalto.cs.apluscourses.utils.observable;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class ObservableCachedReadOnlyPropertyTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testObservableCachedReadOnlyProperty() {
    var supplier = (Supplier<Object>) mock(Supplier.class);
    var supplied = new Object();
    when(supplier.get()).thenReturn(supplied);
    var observable = new ObservableCachedReadOnlyProperty<>(supplier);
    assertSame(supplied, observable.get());
    observable.get();
    verify(supplier, times(1)).get();
    assertSame(supplied, observable.get());
    verifyNoMoreInteractions(supplier);
  }
}
