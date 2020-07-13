package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public interface ValidationItem {
  @Nullable
  ValidationError validate();

  @Nullable
  default JComponent getComponent() {
    return null;
  }
}
