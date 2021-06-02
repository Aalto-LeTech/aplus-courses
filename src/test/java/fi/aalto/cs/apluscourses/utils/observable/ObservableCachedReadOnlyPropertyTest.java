package fi.aalto.cs.apluscourses.utils.observable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Supplier;
import org.junit.Test;

public class ObservableCachedReadOnlyPropertyTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testObservableCachedReadOnlyProperty() {
    var supplier = (Supplier<Object>) mock(Supplier.class);
    var observable = new ObservableCachedReadOnlyProperty<>(supplier);
    observable.get();
    observable.get();
    verify(supplier, times(1)).get();
    observable.get();
    verifyNoMoreInteractions(supplier);
  }
}
