package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ConsListTest {

  @Test
  public void testGet() {
    List<String> weekdays = List.of("mon", "tue", "wed", "thu", "fri", "sat");
    List<String> sundayStartingWeekdays = new ConsList<>("sun", weekdays);

    assertEquals("sun", sundayStartingWeekdays.get(0));
    assertEquals("mon", sundayStartingWeekdays.get(1));
    assertEquals("sat", sundayStartingWeekdays.get(6));
  }

  @Test
  public void testSize() {
    List<Integer> factorials = List.of(1, 2, 6, 24, 120);
    List<Integer> factorialsWithZero = new ConsList<>(1, factorials);

    assertEquals(6, factorialsWithZero.size());
  }
}
