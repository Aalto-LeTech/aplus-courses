package fi.aalto.cs.apluscourses.utils.async;

import org.jetbrains.annotations.Nullable;

// For testing/debugging purposes
class DelayTaskManager implements TaskManager<Runnable> {

  @Override
  public Runnable fork(Runnable runnable) {
    return runnable;
  }

  @Override
  public void join(@Nullable Runnable task) {
    if (task != null) {
      task.run();
    }
  }
}
