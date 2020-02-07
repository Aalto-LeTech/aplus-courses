package fi.aalto.cs.apluscourses.presentation.base;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class BaseViewModel<T> {
  final AtomicBoolean hasChangedSinceLastCheck = new AtomicBoolean(true);
  private final T model;

  public BaseViewModel(@NotNull T model) {
    this.model = model;
  }

  protected void onChanged() {
    // Default implementation: do nothing.
  }

  public boolean checkIfChanged() {
    return hasChangedSinceLastCheck.getAndSet(false);
  }

  public void changed() {
    hasChangedSinceLastCheck.set(true);
    onChanged();
  }

  @NotNull
  public T getModel() {
    return model;
  }
}
