package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import fi.aalto.cs.apluscourses.utils.OptionalBooleanLogic;
import fi.aalto.cs.apluscourses.utils.Tree;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SelectableNodeViewModel<T> extends BaseViewModel<T> implements Tree {

  @NotNull
  private final List<SelectableNodeViewModel<?>> children;
  private volatile boolean visibility = true;

  private volatile boolean selected = false;

  public SelectableNodeViewModel(@NotNull T model,
                                 @Nullable List<SelectableNodeViewModel<?>> children) {
    super(model);
    this.children = Optional.ofNullable(children).orElse(Collections.emptyList());
  }

  /**
   * Applies a filter to this node, that is, sets the node visible if the filter applies to the node
   * or one of its descendants, or the filter is not applicable to this node.  Otherwise, sets the
   * node invisible.
   *
   * @param filter A filter.
   * @return True, if the filter applies to this node or one of its descendants, otherwise false.
   */
  public Optional<Boolean> applyFilter(Filter filter) {
    Optional<Boolean> myResult = filter.apply(this);
    Optional<Boolean> result = myResult;
    for (SelectableNodeViewModel<?> child : children) {
      if (Thread.currentThread().isInterrupted()) {
        return Optional.empty();
      }
      result = OptionalBooleanLogic.or(result, child.applyFilter(filter));
    }
    result = OptionalBooleanLogic.and(result, myResult);
    visibility = result.orElse(true);
    return result;
  }

  public abstract long getId();

  public boolean isVisible() {
    return visibility;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  @NotNull
  public List<SelectableNodeViewModel<?>> getChildren() {
    return children;
  }

  @NotNull
  public Stream<? extends SelectableNodeViewModel<?>> streamVisibleChildren() {
    return children.stream().filter(SelectableNodeViewModel::isVisible);
  }
}
