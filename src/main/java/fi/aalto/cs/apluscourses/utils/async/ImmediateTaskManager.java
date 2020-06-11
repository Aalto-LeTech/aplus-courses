package fi.aalto.cs.apluscourses.utils.async;

import org.jetbrains.annotations.Nullable;

// For testing/debugging purposes
public class ImmediateTaskManager implements TaskManager<Void> {
  @Override
  public Void fork(Runnable runnable) {
    runnable.run();
    return null;
  }

  @Override
  public void join(@Nullable Void task) {
    // do nothing
  }
}
