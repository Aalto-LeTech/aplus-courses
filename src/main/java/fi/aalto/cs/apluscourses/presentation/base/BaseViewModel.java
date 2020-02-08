package fi.aalto.cs.apluscourses.presentation.base;

import org.jetbrains.annotations.NotNull;

public class BaseViewModel<T> {
  private final T model;

  public BaseViewModel(@NotNull T model) {
    this.model = model;
  }

  public void onChanged() {
    // Default implementation: do nothing.
  }

  @NotNull
  public T getModel() {
    return model;
  }
}
