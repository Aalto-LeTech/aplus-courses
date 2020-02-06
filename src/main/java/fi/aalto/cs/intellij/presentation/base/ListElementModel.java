package fi.aalto.cs.intellij.presentation.base;

import org.jetbrains.annotations.NotNull;

public class ListElementModel<T> extends BaseModel<T> {

  private volatile boolean selected;
  private volatile BaseListModel<?> listModel;
  private volatile int index;

  public ListElementModel(@NotNull T model) {
    super(model);
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  protected void onChanged() {
    BaseListModel<?> localListModel = listModel;
    if (localListModel != null) {
      localListModel.onElementChanged(getIndex());
    }
  }

  public boolean isSelected() {
    return selected;
  }

  public void setListModel(BaseListModel<?> listModel) {
    this.listModel = listModel;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}
