package fi.aalto.cs.apluscourses.presentation.filter;

import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.RestartableTask;
import org.jetbrains.annotations.NotNull;

public class FilterEngine {
  public final @NotNull Event filtered = new Event();
  private final @NotNull RestartableTask filterTask = new RestartableTask(this::applyFilter, filtered::trigger);
  private final @NotNull Filterable filterable;
  private final @NotNull Options options;

  /**
   * Provides a collection view model with filtering.
   */
  public FilterEngine(@NotNull Options options, @NotNull Filterable target) {
    this.options = options;
    this.filterable = target;
    options.optionsChanged.addListener(this, FilterEngine::filter);
  }

  public void filter() {
    filterTask.restart();
  }

  private void applyFilter() throws InterruptedException {
    filterable.applyFilter(options);
  }
}
