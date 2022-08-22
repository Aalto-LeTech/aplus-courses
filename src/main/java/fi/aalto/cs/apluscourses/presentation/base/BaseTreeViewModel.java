package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import fi.aalto.cs.apluscourses.presentation.filter.FilterEngine;
import fi.aalto.cs.apluscourses.presentation.filter.Filterable;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseTreeViewModel<T> extends SelectableNodeViewModel<T> implements Filterable {

  private final @NotNull FilterEngine filterEngine;

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
    filterEngine = new FilterEngine(options, this);
    filterEngine.filter();
  }

  public @NotNull FilterEngine getFilterEngine() {
    return filterEngine;
  }

  /**
   * Base class for tree view models.
   */
  public BaseTreeViewModel(@NotNull T model,
                           @Nullable List<SelectableNodeViewModel<?>> children) {
    this(model, children, Options.EMPTY);
  }

  @Override
  public long getId() {
    return 0;
  }

  @NotNull
  public Selection findSelected() {
    return new Selection(traverseAndFind(SelectableNodeViewModel::isSelected));
  }

  @Override
  public void applyFilter(@NotNull Filter filter) throws InterruptedException {
    applyFilterRecursive(filter);
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
