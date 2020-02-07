package fi.aalto.cs.apluscourses.presentation.base;

import org.jetbrains.annotations.NotNull;

public class ListElementViewModel<T> extends BaseViewModel<T> {

  private volatile boolean selected;
  private volatile BaseListViewModel<?> listModel;
  private volatile int index;

  public ListElementViewModel(@NotNull T model) {
    super(model);
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  protected void onChanged() {
    BaseListViewModel<?> localListModel = listModel;
    if (localListModel != null) {
      localListModel.onElementChanged(getIndex());
    }
  }

  public boolean isSelected() {
    return selected;
  }

  public void setListModel(BaseListViewModel<?> listModel) {
    this.listModel = listModel;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}
