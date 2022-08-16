package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import fi.aalto.cs.apluscourses.utils.Event;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ListElementViewModel<T> extends BaseViewModel<T> {

  protected volatile boolean visibility = true;

  public final Event changed = new Event();

  private volatile boolean selected;

  public ListElementViewModel(@NotNull T model) {
    super(model);
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  public void onChanged() {
    changed.trigger();
  }

  public void applyFilter(@NotNull Filter filter) {
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
}
