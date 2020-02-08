package fi.aalto.cs.apluscourses.presentation.base;

import org.jetbrains.annotations.NotNull;

public class ListElementViewModel<T> extends BaseViewModel<T> {

  private volatile boolean selected;
  // Sonar does not like non-primitive volatile fields because the semantics of "volatile" are
  // easily misunderstood by programmers but we now what we are doing here.
  private volatile BaseListViewModel<?> listModel; //NOSONAR
  private volatile int index;

  public ListElementViewModel(@NotNull T model) {
    super(model);
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  public void onChanged() {
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
