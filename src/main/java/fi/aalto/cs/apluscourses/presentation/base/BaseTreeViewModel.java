package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.Event;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTreeViewModel<T>
    extends SelectableNodeViewModel<T> implements TreeViewModel {

  @NotNull
  public final Event filtered = new Event();
  @NotNull
  protected final Options options;
  @Nullable
  private Thread filterThread = null;
  @NotNull
  private final Object filterLock = new Object();

  /**
   * Base class for tree view models.
   */
  public BaseTreeViewModel(@NotNull T model,
                           @Nullable List<SelectableNodeViewModel<?>> children,
                           @NotNull Options options) {
    super(model, children);
    this.options = options;
    this.options.optionsChanged.addListener(this, BaseTreeViewModel::filter);
  }

  protected void filter() {
    synchronized (filterLock) {
      filterThread = new FilterThread(filterThread);
      filterThread.start();
    }
  }

  @NotNull
  public Options getFilterOptions() {
    return options;
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
        applyFilter(options);
        filtered.trigger();
      } catch (InterruptedException e) {
        interrupt();
      }
    }
  }
}
