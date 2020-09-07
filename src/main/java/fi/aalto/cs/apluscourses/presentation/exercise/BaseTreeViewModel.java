package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.Event;
import java.util.List;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseTreeViewModel<T>
    extends SelectableNodeViewModel<T> implements TreeViewModel {

  @NotNull
  public final Event filtered = new Event();
  @NotNull
  protected final Executor filterExecutor;
  @NotNull
  protected final Options options;

  /**
   * Base class for tree view models.
   */
  public BaseTreeViewModel(@NotNull T model,
                           @Nullable List<SelectableNodeViewModel<?>> children,
                           @NotNull Options options,
                           @NotNull Executor filterExecutor) {
    super(model, children);
    this.filterExecutor = filterExecutor;
    this.options = options;
    this.options.optionsChanged.addListener(this, BaseTreeViewModel::filter);
  }

  protected void filter() {
    filterExecutor.execute(this::filterInBackground);
  }

  private void filterInBackground() {
    applyFilter(options);
    filtered.trigger();
  }

  @NotNull
  public Options getFilterOptions() {
    return options;
  }
}
