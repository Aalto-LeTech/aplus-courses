package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class CourseSelectionViewModel {

  private List<CourseItemViewModel> courses;

  public final ObservableProperty<String> selectedCourseUrl
      = new ObservableReadWriteProperty<>("", CourseSelectionViewModel::validateCourseUrl);

  public CourseSelectionViewModel(List<CourseItemViewModel> courses) {
    this.courses = courses;
  }

  public CourseItemViewModel[] getCourses() {
    return courses.toArray(new CourseItemViewModel[0]);
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
