package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterEngine {

  public final @Nullable Callback callback;
  private final @NotNull FilterApplier applier;
  @Nullable
  protected final Filter options;
  @Nullable
  private Thread thread = null;
  @NotNull
  private final Object lock = new Object();

  public interface Callback {
    void trigger();
  }

  /**
   * Constructor.
   */
  public FilterEngine(@Nullable Filter filterOptions, @NotNull FilterApplier applier, @Nullable Callback callback) {
    this.callback = callback;
    options = filterOptions;
    this.applier = applier;
  }

  /**
   * Method that is being called from the view models and triggers the filtering.
   */
  public void filter() {
    synchronized (this.lock) {
      thread = new FilterThread(thread);
      thread.start();
    }
  }

  private class FilterThread extends Thread {

    @Nullable
    private final Thread previous;

    public FilterThread(@Nullable Thread previous) {
      this.previous = previous;
    }

    @Override
    public void run() {
      try {
        if (previous != null) {
          previous.interrupt();
          previous.join();
        }
        if (options != null) {
          applier.applyFilter(options);
        }
        if (done() && callback != null) {
          callback.trigger();
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

  @FunctionalInterface
  public interface FilterApplier {
    void applyFilter(@NotNull Filter filter) throws InterruptedException;
  }
}
