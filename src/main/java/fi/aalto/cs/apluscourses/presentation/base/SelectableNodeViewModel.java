package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
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
  protected volatile boolean visibility = true;

  private volatile boolean selected = false;

  protected SelectableNodeViewModel(@NotNull T model,
                                 @Nullable List<SelectableNodeViewModel<?>> children) {
    super(model);
    this.children = Optional.ofNullable(children).orElse(Collections.emptyList());
  }

  /**
   * Applies a filter to this node, that is, sets the node visible if the filter applies to the node
   * or one of its descendants, or the filter is not applicable to this node.  Otherwise, sets the
   * node invisible. Some nodes with no visible children become invisible according to
   * setVisibilityByFilterResult
   *
   * @param filter A filter.
   * @return True, if the filter applies to this node or one of its descendants, otherwise false.
   */
  public Optional<Boolean> applyFilter(Filter filter) throws InterruptedException {
    Optional<Boolean> result = filter.apply(this);
    if (result.isEmpty() || Boolean.TRUE.equals(result.get())) {
      for (SelectableNodeViewModel<?> child : children) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        Optional<Boolean> childResult = child.applyFilter(filter);
        if (childResult.isPresent()) {
          result = Optional.of(result.orElse(false) || Boolean.TRUE.equals(childResult.get()));
        }
      }
    }
    setVisibilityByFilterResult(result);
    return result;
  }

  protected void setVisibilityByFilterResult(Optional<Boolean> result) {
    visibility = result.orElse(true);
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
