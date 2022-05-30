package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ListElementViewModel<T> extends BaseViewModel<T> {

  protected volatile boolean visibility = true;

  private volatile boolean selected;
  // Sonar does not like non-primitive volatile fields because the semantics of "volatile" are
  // easily misunderstood by programmers but we know what we are doing here.
  // What is more, Sonar dislikes wildcard type parameters elsewhere than parameters, and that's
  // another thing where Sonar is overly-cautious.
  private volatile BaseListViewModel<?> listModel; //  NOSONAR
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

  public void applyFilter(Filter filter) {
    setVisibilityByFilterResult(filter.apply(this));
  }

  protected void setVisibilityByFilterResult(Optional<Boolean> result) {
    visibility = result.orElse(true);
  }

  public boolean isVisible() {
    return visibility;
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
