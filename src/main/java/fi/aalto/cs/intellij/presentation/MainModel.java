package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.ObservableProperty;
import org.jetbrains.annotations.NotNull;

public class MainModel {
  @NotNull
  public final ObservableProperty<CourseModel> course = new ObservableProperty<>(null);
}
