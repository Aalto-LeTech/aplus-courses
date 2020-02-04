package fi.aalto.cs.intellij.presentation.common;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class BaseModel<T> {
  final AtomicBoolean hasChangedSinceLastCheck = new AtomicBoolean(true);
  private final T model;

  public BaseModel(@NotNull T model) {
    this.model = model;
  }

  protected void onChanged() {

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
