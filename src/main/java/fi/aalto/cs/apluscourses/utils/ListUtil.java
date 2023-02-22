package fi.aalto.cs.apluscourses.utils;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ListUtil {
  private ListUtil() {

  }

  /**
   * Appends two lists onto a new one.
   */
  public static <T> List<T> appendTwoLists(@NotNull List<T> first, @NotNull List<T> second) {
    var both = new ArrayList<T>(first.size() + second.size());
    both.addAll(first);
    both.addAll(second);
    return both;
  }

  /**
   * Calls the given action for each item in the list.
   */
  public static <T> void forEachWithIndex(@NotNull List<T> list,
                                          @NotNull IndexedConsumer<? super T> action) {
    for (int i = 0; i < list.size(); i++) {
      action.accept(list.get(i), i);
    }
  }

  @FunctionalInterface
  public interface IndexedConsumer<T> {
    void accept(T item, int index);
  }
}
