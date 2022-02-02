package fi.aalto.cs.apluscourses.presentation;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class CourseItemViewModel {

  private final String name;
  private final String semester;
  private final String url;

  /**
   * Construct a view model with the given parameters.
   */
  public CourseItemViewModel(@NotNull String name, @NotNull String semester, @NotNull String url) {
    this.name = name;
    this.semester = semester;
    this.url = url;
  }

  public static CourseItemViewModel fromMap(@NotNull Map<String, String> course) {
    return new CourseItemViewModel(course.get("name"), course.get("semester"), course.get("url"));
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public String getSemester() {
    return semester;
  }

  @NotNull
  public String getUrl() {
    return url;
  }

}
