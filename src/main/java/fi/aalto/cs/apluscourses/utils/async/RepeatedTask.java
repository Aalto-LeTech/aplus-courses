package fi.aalto.cs.apluscourses.utils.async;

/**
 * Represents a task that gets run repeatedly with a specific time interval between each run.
 * Subclasses need only implement the task and this class takes care of the rest. This class is
 * thread safe.
 */
public abstract class RepeatedTask {

  private final long updateInterval;

  private Thread thread = null;

  public RepeatedTask(long updateInterval) {
    this.updateInterval = updateInterval;
  }

  /**
   * Starts or restarts the repeater. In practice this means that the task is run very soon, since
   * the interval is reset. This method is thread safe and can be called from multiple threads.
   */
  public synchronized void restart() {
    if (thread != null) {
      thread.interrupt();
    }
    thread = new Thread(this::run);
    thread.start();
  }

  /**
   * Requests that the repeater stops, or does nothing if it isn't running. This method is thread
   * safe. Note, that the task might not stop immediately.
   */
  public synchronized void stop() {
    if (thread != null) {
      thread.interrupt();
    }
  }

  /**
   * This method implements the task that is repeated at the set interval. If the task takes a long
   * time, then it should periodically check for interruptions using Thread.interrupted() and exit
   * if it has been interrupted.
   */
  protected abstract void doTask();

  /*
   * The run lock ensures that only one thread executes the 'run' method. The run lock guarantees
   * that a new thread created by 'restart' blocks until the previous thread has received the
   * interrupt and exited. Note, that making the 'run' method 'synchronized' would cause a deadlock,
   * because 'restart' is synchronized as well.
   */
  private final Object runLock = new Object();

  private void run() {
    synchronized (runLock) {
      while (true) { //  NOSONAR
        try {
          doTask();
          Thread.sleep(updateInterval); //  NOSONAR
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

}
