package fi.aalto.cs.apluscourses.utils.observable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObservableCachedReadOnlyPropertyTest {
  @SuppressWarnings("unchecked")
  @Test
  void testObservableCachedReadOnlyProperty() {
    var supplier = (Supplier<Object>) mock(Supplier.class);
    var supplied = new Object();
    when(supplier.get()).thenReturn(supplied);
    var observable = new ObservableCachedReadOnlyProperty<>(supplier);
    Assertions.assertSame(supplied, observable.get());
    observable.get();
    verify(supplier, times(1)).get();
    Assertions.assertSame(supplied, observable.get());
    verifyNoMoreInteractions(supplier);
  }
}
