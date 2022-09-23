package fi.aalto.cs.apluscourses.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RestartableTask {

  private final @NotNull InterruptibleTask task;
  private final @Nullable Runnable onFinish;
  @Nullable
  private InterruptingThread thread = null;
  @NotNull
  private final Object lock = new Object();

  /**
   * A routine that can be restarted, again and again.
   * When it is restarted, the previous invocation, if still running, is interrupted.
   * No more than one invocation can be running at a time.
   * onFinish is called each time the task gets completed without interruption.
   * Note that new invocations of the task might, however, start before onFinish is called.
   */
  public RestartableTask(@NotNull InterruptibleTask task,
                         @Nullable Runnable onFinish) {
    this.task = task;
    this.onFinish = onFinish;
  }

  /**
   * Call this to (re-)start the task.
   */
  public Thread restart() {
    synchronized (lock) {
      thread = new InterruptingThread(thread);
      thread.start();
      return thread;
    }
  }

  private static void interruptAndJoin(@Nullable InterruptingThread thread) throws InterruptedException {
    if (thread != null) {
      interruptAndJoin(thread.previous);
      thread.interrupt();
      thread.join();
    }
  }

  private class InterruptingThread extends Thread {

    @Nullable
    private volatile InterruptingThread previous;

    public InterruptingThread(@Nullable InterruptingThread previous) {
      this.previous = previous;
    }

    @Override
    public void run() {
      try {
        interruptAndJoin(previous);
        previous = null;
        task.run();
        if (done() && onFinish != null) {
          onFinish.run();
        }
      } catch (InterruptedException e) {
        interrupt();
      }
    }

    private boolean done() {
      synchronized (lock) {
        if (thread == this) {
          thread = null;
          return true;
        }
      }
      return false;
    }
  }
}
