package fi.aalto.cs.intellij.presentation.common;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaseModel {
  final AtomicBoolean hasChangedSinceLastCheck = new AtomicBoolean(true);

  protected void onChanged() {

  }

  public boolean checkIfChanged() {
    return hasChangedSinceLastCheck.getAndSet(false);
  }

  public void changed() {
    hasChangedSinceLastCheck.set(true);
    onChanged();
  }
}
