package fi.aalto.cs.apluscourses.utils;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class CommonUtil {
  private CommonUtil() {

  }

  /**
   * Takes two arrays and returns an array containing distinct elements of them.
   *
   * @param arr1 An array.
   * @param arr2 Another array.
   * @param generator Function that generates the resulting array object, most likely
   *                  <code>T[]::new</code> where T is the actual type.
   * @param <T> Type of the array elements.
   * @return An array with distinct elements.
   */
  public static <T> T[] unionArrays(T[] arr1, T[] arr2, IntFunction<T[]> generator) {
    return Stream.concat(Arrays.stream(arr1), Arrays.stream(arr2))
        .distinct()
        .toArray(generator);
  }
}
