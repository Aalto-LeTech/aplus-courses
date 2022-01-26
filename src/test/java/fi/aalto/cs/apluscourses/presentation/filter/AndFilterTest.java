package fi.aalto.cs.apluscourses.presentation.filter;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AndFilterTest {

  final Filter yes = item -> Optional.of(true);
  final Filter no = item -> Optional.of(false);
  final Filter nil = item -> Optional.empty();
  final Filter err = item -> {
    throw new UnsupportedOperationException();
  };

  @Test
  void testAndFilter() {
    Filter andFilter1 = new AndFilter(List.of(nil, nil, nil));
    Assertions.assertEquals(Optional.empty(), andFilter1.apply(new Object()));

    Filter andFilter2 = new AndFilter(List.of(nil, no, nil));
    Assertions.assertEquals(Optional.of(false), andFilter2.apply(new Object()));

    Filter andFilter3 = new AndFilter(List.of(nil, no, yes));
    Assertions.assertEquals(Optional.of(false), andFilter3.apply(new Object()));

    Filter andFilter4 = new AndFilter(List.of(yes, no, yes));
    Assertions.assertEquals(Optional.of(false), andFilter4.apply(new Object()));

    Filter andFilter5 = new AndFilter(List.of(nil, yes, yes));
    Assertions.assertEquals(Optional.of(true), andFilter5.apply(new Object()));

    Filter andFilter6 = new AndFilter(List.of(nil, nil, yes));
    Assertions.assertEquals(Optional.of(true), andFilter6.apply(new Object()));

    // short-circuit
    Filter andFilter7 = new AndFilter(List.of(nil, no, err));
    Assertions.assertEquals(Optional.of(false), andFilter7.apply(new Object()));

    // can't short-circuit -> err
    Filter andFilter8 = new AndFilter(List.of(nil, yes, err));
    Exception exception = null;
    try {
      andFilter8.apply(new Object());
    } catch (UnsupportedOperationException e) {
      exception = e;
    }
    Assertions.assertNotNull(exception);
  }
}
