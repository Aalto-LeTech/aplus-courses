package fi.aalto.cs.apluscourses.utils.async;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a task that gets run repeatedly with a specific time interval between each run.
 * Subclasses need only implement the task and this class takes care of the rest. This class is
 * thread safe.
 */
public abstract class RepeatedTask {

  private final long updateInterval;

  private final ScheduledExecutorService executorService
      = Executors.newSingleThreadScheduledExecutor();

  private ScheduledFuture<?> runningTask = null;

  public RepeatedTask(long updateInterval) {
    this.updateInterval = updateInterval;
  }

  /**
   * Starts or restarts the repeater. In practice this means that the task is run very soon, since
   * the interval is reset. This method is thread safe and can be called from multiple threads.
   */
  public synchronized void restart() {
    if (runningTask != null) {
      runningTask.cancel(true);
    }
    runningTask = executorService.scheduleAtFixedRate(
        this::doTask, 0, updateInterval, TimeUnit.MILLISECONDS
    );
  }

  /**
   * Requests that the repeater stops, or does nothing if it isn't running. This method is thread
   * safe. Note, that the task might not stop immediately.
   */
  public synchronized void stop() {
    executorService.shutdownNow();
  }

  /**
   * This method implements the task that is repeated at the set interval. If the task takes a long
   * time, then it should periodically check for interruptions using Thread.interrupted() and exit
   * if it has been interrupted.
   */
  protected abstract void doTask();

}
