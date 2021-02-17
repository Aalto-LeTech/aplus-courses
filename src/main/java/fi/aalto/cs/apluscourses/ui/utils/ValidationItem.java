package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ValidationItem {
  @Nullable
  ValidationError validate();

  @Nullable
  default JComponent getComponent() {
    return null;
  }
}
