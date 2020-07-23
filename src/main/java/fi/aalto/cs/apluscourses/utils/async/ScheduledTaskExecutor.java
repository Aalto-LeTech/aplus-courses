package fi.aalto.cs.apluscourses.utils.async;

import com.intellij.util.concurrency.AppExecutorUtil;
import java.util.concurrent.TimeUnit;

public class ScheduledTaskExecutor {

  /**
   * A simple wrapper on the IntelliJ IDEA's Executors class. Does nothing but scheduling a task to
   * be run at regular intervals.
   *
   * @param runnable a {@link Runnable} task to execute.
   * @param startAfter an interval to start the task after.
   * @param repeatEvery an interval to repeat the task every.
   * @param timeUnit a time unit to apply to previous 2 (two) parameters.
   */
  public ScheduledTaskExecutor(Runnable runnable, long startAfter, long repeatEvery,
      TimeUnit timeUnit) {
    AppExecutorUtil.getAppScheduledExecutorService()
        .scheduleWithFixedDelay(runnable, startAfter, repeatEvery, timeUnit);
    //todo: incremental interval how-to?
  }
}
