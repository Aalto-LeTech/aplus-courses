package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SelectableNodeViewModel<T> extends BaseViewModel<T>
    implements TreeViewModel, Filterable {

  @NotNull
  private final List<SelectableNodeViewModel<?>> children;
  public final ObservableProperty<Boolean> isVisible = new ObservableReadWriteProperty<>(true);

  private volatile boolean selected = false;

  public SelectableNodeViewModel(@NotNull T model,
                                 @Nullable List<SelectableNodeViewModel<?>> children) {
    super(model);
    this.children = Optional.ofNullable(children).orElse(Collections.emptyList());
  }

  public boolean applyFilter(Filter filter) {
    return isVisible.set(
        children.stream().anyMatch(child -> child.applyFilter(filter)) || filter.apply(this));
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

  @Override
  public void addVisibilityListener(Listener listener) {
    isVisible.addValueObserver(listener, Listener::visibilityChanged);
  }
}
