package fi.aalto.cs.apluscourses.presentation.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;

public class AndFilterTest {

  final Filter yes = item -> Optional.of(true);
  final Filter no = item -> Optional.of(false);
  final Filter nil = item -> Optional.empty();
  final Filter err = item -> {
    throw new UnsupportedOperationException();
  };

  @Test
  public void testAndFilter() {
    Filter andFilter1 = new AndFilter(Arrays.asList(nil, nil, nil));
    assertEquals(Optional.empty(), andFilter1.apply(new Object()));

    Filter andFilter2 = new AndFilter(Arrays.asList(nil, no, nil));
    assertEquals(Optional.of(false), andFilter2.apply(new Object()));

    Filter andFilter3 = new AndFilter(Arrays.asList(nil, no, yes));
    assertEquals(Optional.of(false), andFilter3.apply(new Object()));

    Filter andFilter4 = new AndFilter(Arrays.asList(yes, no, yes));
    assertEquals(Optional.of(false), andFilter4.apply(new Object()));

    Filter andFilter5 = new AndFilter(Arrays.asList(nil, yes, yes));
    assertEquals(Optional.of(true), andFilter5.apply(new Object()));

    Filter andFilter6 = new AndFilter(Arrays.asList(nil, nil, yes));
    assertEquals(Optional.of(true), andFilter6.apply(new Object()));

    // short-circuit
    Filter andFilter7 = new AndFilter(Arrays.asList(nil, no, err));
    assertEquals(Optional.of(false), andFilter7.apply(new Object()));

    // can't short-circuit -> err
    Filter andFilter8 = new AndFilter(Arrays.asList(nil, yes, err));
    Exception exception = null;
    try {
      andFilter8.apply(new Object());
    } catch (UnsupportedOperationException e) {
      exception = e;
    }
    assertNotNull(exception);
  }
}
