package fi.aalto.cs.apluscourses.presentation.filter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Test;

public class LogicalFilterTest {

  final Filter yes = item -> Optional.of(true);
  final Filter no = item -> Optional.of(false);
  final Filter nil = item -> Optional.empty();

  @Test
  public void testConjunction() {
    Filter conjunction1 = new LogicalFilter.Conjunction(Arrays.asList(nil, nil, nil));
    assertEquals(Optional.empty(), conjunction1.apply(new Object()));

    Filter conjunction2 = new LogicalFilter.Conjunction(Arrays.asList(nil, no, nil));
    assertEquals(Optional.of(false), conjunction2.apply(new Object()));

    Filter conjunction3 = new LogicalFilter.Conjunction(Arrays.asList(nil, no, yes));
    assertEquals(Optional.of(false), conjunction3.apply(new Object()));

    Filter conjunction4 = new LogicalFilter.Conjunction(Arrays.asList(yes, no, yes));
    assertEquals(Optional.of(false), conjunction4.apply(new Object()));

    Filter conjunction5 = new LogicalFilter.Conjunction(Arrays.asList(nil, yes, yes));
    assertEquals(Optional.of(true), conjunction5.apply(new Object()));

    Filter conjunction6 = new LogicalFilter.Conjunction(Arrays.asList(nil, nil, yes));
    assertEquals(Optional.of(true), conjunction6.apply(new Object()));
  }

}
