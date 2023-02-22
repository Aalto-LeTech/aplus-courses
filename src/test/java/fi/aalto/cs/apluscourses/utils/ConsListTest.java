package fi.aalto.cs.apluscourses.utils;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConsListTest {

  @Test
  void testGet() {
    List<String> weekdays = List.of("mon", "tue", "wed", "thu", "fri", "sat");
    List<String> sundayStartingWeekdays = new ConsList<>("sun", weekdays);

    Assertions.assertEquals("sun", sundayStartingWeekdays.get(0));
    Assertions.assertEquals("mon", sundayStartingWeekdays.get(1));
    Assertions.assertEquals("sat", sundayStartingWeekdays.get(6));
  }

  @Test
  void testSize() {
    List<Integer> factorials = List.of(1, 2, 6, 24, 120);
    List<Integer> factorialsWithZero = new ConsList<>(1, factorials);

    Assertions.assertEquals(6, factorialsWithZero.size());
  }
}
