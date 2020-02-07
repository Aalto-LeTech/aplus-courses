package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.utils.ObservableProperty;
import org.jetbrains.annotations.NotNull;

public class MainViewModel {
  @NotNull
  public final ObservableProperty<CourseViewModel> course = new ObservableProperty<>(null);
}
