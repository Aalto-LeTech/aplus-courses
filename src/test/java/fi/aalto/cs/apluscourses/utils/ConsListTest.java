package fi.aalto.cs.apluscourses.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConsListTest {

  @Test
  public void testGet() {
    List<String> weekdays = Arrays.asList("mon", "tue", "wed", "thu", "fri", "sat");
    List<String> sundayStartingWeekdays = new ConsList<>("sun", weekdays);

    assertEquals("sun", sundayStartingWeekdays.get(0));
    assertEquals("mon", sundayStartingWeekdays.get(1));
    assertEquals("sat", sundayStartingWeekdays.get(6));
  }

  @Test
  public void testSize() {
    List<Integer> factorials = Arrays.asList(1, 2, 6, 24, 120);
    List<Integer> factorialsWithZero = new ConsList<>(1, factorials);

    assertEquals(6, factorialsWithZero.size());
  }
}
