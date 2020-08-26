package fi.aalto.cs.apluscourses.presentation.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SelectableNodeViewModel<T, C> extends BaseViewModel<T>
    implements TreeViewModel {

  private volatile boolean visible = true;

  private volatile boolean selected = false;

  public SelectableNodeViewModel(@NotNull T model, @Nullable List<SelectableNodeViewModel<C>>) {
    super(model);
  }

  public void applyFilter(BaseFilter<T> filter) {
    visible = filter.apply(getModel());
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
}
