package fi.aalto.cs.intellij.utils;

import java.util.List;

public interface TaskManager<T> {
  T fork(Runnable runnable);

  void join(T task);

  default void joinAll(List<T> tasks) {
    for (T task : tasks) {
      join(task);
    }
  }
}
