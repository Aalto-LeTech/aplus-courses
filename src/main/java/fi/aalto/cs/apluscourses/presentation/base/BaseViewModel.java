package fi.aalto.cs.apluscourses.presentation.base;

import org.jetbrains.annotations.NotNull;

public class BaseViewModel<T> {
  protected final T model;

  public BaseViewModel(@NotNull T model) {
    this.model = model;
  }

  public void onChanged() {
    // Default implementation: do nothing.
  }

  public T getModel() {
    return model;
  }
}
