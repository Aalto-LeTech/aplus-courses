package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class ArrayUtilTest {

  @Test
  void testMapArray() {
    Integer[] numbers = new Integer[] {2, 16, 50, 70};
    String[] result = ArrayUtil.mapArray(numbers, Integer::toHexString, String[]::new);

    MatcherAssert.assertThat(result, is(new String[] {"2", "10", "32", "46"}));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testMapArrayThrows() {
    ArrayUtil.ThrowingFunction<String, Void, UnsupportedOperationException> func
        = mock(ArrayUtil.ThrowingFunction.class);
    doThrow(new UnsupportedOperationException()).when(func).apply("seal");

    String[] animals = new String[] {"dog", "cat", "seal"};

    assertThrows(UnsupportedOperationException.class, () ->
        ArrayUtil.mapArray(animals, func, Void[]::new));
  }
}
