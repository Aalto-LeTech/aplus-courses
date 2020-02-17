package fi.aalto.cs.apluscourses.utils.async;

import org.jetbrains.annotations.Nullable;

public class SimpleAsyncTaskManager implements TaskManager<Thread> {


  @Override
  public Thread fork(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.start();
    return thread;
  }

  @Override
  public void join(@Nullable Thread task) {
    if (task == null) {
      return;
    }
    try {
      task.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
