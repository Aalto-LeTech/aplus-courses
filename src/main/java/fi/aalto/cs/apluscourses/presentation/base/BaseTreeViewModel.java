package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.RestartableTask;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseTreeViewModel<T> extends SelectableNodeViewModel<T> {

  @NotNull
  public final Event filtered = new Event();
  private final @NotNull RestartableTask filterTask;
  private final @NotNull Options options;
  @Nullable
  protected volatile SelectableNodeViewModel<?> selectedItem = null; //  NOSONAR

  public void setSelectedItem(@Nullable SelectableNodeViewModel<?> selectedItem) {
    this.selectedItem = selectedItem;
  }

  public @Nullable SelectableNodeViewModel<?> getSelectedItem() {
    return selectedItem;
  }

  /**
   * Base class for tree view models.
   */
  public BaseTreeViewModel(@NotNull T model,
                           @Nullable List<SelectableNodeViewModel<?>> children,
                           @NotNull Options options) {
    super(model, children);
    this.filterTask = new RestartableTask(this::doFilter, filtered::trigger);
    this.options = options;
    options.optionsChanged.addListener(this, BaseTreeViewModel::filter);
    filter();
  }

  private void filter() {
    filterTask.restart();
  }

  /**
   * Base class for tree view models.
   */
  public BaseTreeViewModel(@NotNull T model,
                           @Nullable List<SelectableNodeViewModel<?>> children) {
    this(model, children, null);
  }

  @Override
  public long getId() {
    return 0;
  }

  @NotNull
  public Selection findSelected() {
    return new Selection(traverseAndFind(SelectableNodeViewModel::isSelected));
  }

  private void doFilter() throws InterruptedException {
    applyFilterRecursive(options);
  }

  public static class Selection {
    protected final List<SelectableNodeViewModel<?>> path;

    public Selection(SelectableNodeViewModel<?>... pathToSelected) {
      this(List.of(pathToSelected));
    }

    public Selection(@Nullable List<SelectableNodeViewModel<?>> pathToSelected) {
      path = pathToSelected;
    }

    @Nullable
    public SelectableNodeViewModel<?> getLevel(int level) {
      return path != null && level < path.size() ? path.get(level) : null;
    }
  }
}
