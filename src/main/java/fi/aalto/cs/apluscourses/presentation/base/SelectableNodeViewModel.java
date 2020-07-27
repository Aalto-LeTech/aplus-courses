package fi.aalto.cs.apluscourses.presentation.base;

import org.jetbrains.annotations.NotNull;

public class SelectableNodeViewModel<T> extends BaseViewModel<T> {
  private volatile boolean selected;

  public SelectableNodeViewModel(@NotNull T model) {
    super(model);
    selected = false;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }
}
