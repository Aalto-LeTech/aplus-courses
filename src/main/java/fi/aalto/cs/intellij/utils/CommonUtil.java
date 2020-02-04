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

  @NotNull
  public static <T> List<T> createList(int length, @NotNull Function<Integer, T> accessByIndex) {
    List<T> list = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      list.add(accessByIndex.apply(i));
    }
    return list;
  }
}
