package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SelectableNodeViewModel<T> extends BaseViewModel<T>
    implements TreeViewModel {

  private final List<SelectableNodeViewModel<?>> children;
  private volatile boolean visible = true;

  private volatile boolean selected = false;

  public SelectableNodeViewModel(@NotNull T model,
                                 @Nullable List<SelectableNodeViewModel<?>> children) {
    super(model);
    this.children = children;
  }

  public boolean applyFilter(Filter filter) {
    return visible = children != null
        && children.parallelStream().anyMatch(child -> child.applyFilter(filter))
        || filter.apply(getModel());
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  @Override
  @NotNull
  public List<SelectableNodeViewModel<?>> getChildren() {
    return Optional.ofNullable(children).orElse(Collections.emptyList());
  }

  @NotNull
  public Stream<SelectableNodeViewModel<?>> streamChildren() {
    return Optional.ofNullable(children).map(List::stream).orElse(Stream.empty());
  }
}
