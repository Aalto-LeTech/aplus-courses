package fi.aalto.cs.apluscourses.presentation;

import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseItemViewModel {

  public enum ProgrammingLanguage {
    Scala,
    Python
  }

  private final @NotNull String name;
  private final @NotNull String semester;
  private final @NotNull String url;
  private final @Nullable ProgrammingLanguage language;

  /**
   * Construct a view model with the given parameters.
   */
  public CourseItemViewModel(@NotNull String name, @NotNull String semester, @NotNull String url,
                             @Nullable String language) {
    this.name = name;
    this.semester = semester;
    this.url = url;
    this.language = getLanguageFromString(language);
  }

  private static ProgrammingLanguage getLanguageFromString(@Nullable String language) {
    if (language == null)
      return null;

    return ProgrammingLanguage.valueOf(language);
  }

  public static CourseItemViewModel fromMap(@NotNull Map<String, String> course) {
    return new CourseItemViewModel(course.get("name"), course.get("semester"), course.get("url"),
        course.get("language"));
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

  @Nullable
  public ProgrammingLanguage getLanguage() { return language; }
}
