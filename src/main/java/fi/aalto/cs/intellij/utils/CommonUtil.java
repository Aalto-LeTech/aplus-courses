package fi.aalto.cs.intellij.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
  private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

  private CommonUtil() {

  }

  /**
   * Creates a new {@link List} of type {@link T} and size {@code length} populated with objects
   * returned by {@code accessByIndex(Integer)}.
   * @param length        The length of the list to be created.
   * @param accessByIndex A function that, when given an integer between 0 and {@code length - 1}
   *                      returns an object of type {@link T}.
   * @param <T>           Type of the elements in the list.
   * @return A {@link List}.
   * @implNote Returned list is an {@link ArrayList}.
   */
  @NotNull
  public static <T> List<T> createList(int length, @NotNull Function<Integer, T> accessByIndex) {
    List<T> list = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      list.add(accessByIndex.apply(i));
    }
    return list;
  }
}
