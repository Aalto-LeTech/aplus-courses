package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class CourseSelectionViewModel {

  @NotNull
  public final ObservableProperty<CourseItemViewModel[]> courses
      = new ObservableReadWriteProperty<>(new CourseItemViewModel[0]);

  public final ObservableProperty<CourseItemViewModel> selectedCourse
      = new ObservableReadWriteProperty<>(null);

  public final ObservableProperty<String> selectedCourseUrl
      = new ObservableReadWriteProperty<>("", CourseSelectionViewModel::validateCourseUrl);

  /**
   * A constructor.
   */
  public CourseSelectionViewModel(List<CourseItemViewModel> courses) {
    this.courses.set(courses.toArray(CourseItemViewModel[]::new));
    selectedCourse.addValueObserver(this, (self, course) ->
        selectedCourseUrl.set(Optional.ofNullable(course)
            .map(CourseItemViewModel::getUrl).orElse("")));
  }

  private static ValidationError validateCourseUrl(String courseUrl) {
    if (courseUrl.trim().isEmpty()) {
      return () -> "Select a course";
    }
    try {
      new URL(courseUrl);
      return null;
    } catch (MalformedURLException e) {
      return () -> "Invalid URL";
    }
  }

}
