package fi.aalto.cs.intellij.presentation.common;

public class ListElementModel extends BaseModel {

  private volatile boolean selected;
  private volatile BaseListModel<? extends ListElementModel> listModel;
  private volatile int index;

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  protected void onChanged() {
    BaseListModel<? extends ListElementModel> localListModel = listModel;
    if (localListModel != null) {
      localListModel.onElementChanged(getIndex());
    }
  }

  public boolean isSelected() {
    return selected;
  }

  public void setListModel(BaseListModel<? extends ListElementModel> listModel) {
    this.listModel = listModel;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}
