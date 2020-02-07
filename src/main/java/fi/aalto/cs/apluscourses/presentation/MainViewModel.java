package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.utils.ObservableProperty;
import org.jetbrains.annotations.NotNull;

public class MainViewModel {

  public MainViewModel() {
    System.out.println("MainViewModel constructed.");
  }

  @NotNull
  public final ObservableProperty<CourseViewModel> courseViewModel = new ObservableProperty<>(null);
}
