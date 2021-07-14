package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Student;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectStudentViewModel {
  @NotNull
  private List<Student> students;
  @NotNull
  private final Course course;
  @NotNull
  private final Authentication authentication;

  @NotNull
  public final ObservableProperty<Student> selectedStudent
      = new ObservableReadWriteProperty<>(null, SelectStudentViewModel::validateSelectedStudent);

  /**
   * A constructor.
   */
  public SelectStudentViewModel(@NotNull List<Student> students,
                                @NotNull Course course,
                                @NotNull Authentication authentication) {
    this.students = students;
    this.course = course;
    this.authentication = authentication;
    sortStudents();
  }

  public void setStudents(@NotNull List<Student> newStudents) {
    students = newStudents;
  }

  /**
   * Sorts the students alphabetically.
   */
  public void sortStudents() {
    students = students
        .stream()
        .sorted(Comparator.comparing(Student::getFullName))
        .collect(Collectors.toList());
  }

  @NotNull
  public List<Student> getStudents() {
    return students;
  }

  @NotNull
  public Course getCourse() {
    return course;
  }

  @NotNull
  public Authentication getAuthentication() {
    return authentication;
  }

  private static @Nullable ValidationError validateSelectedStudent(@Nullable Student student) {
    return student == null ? new StudentNotSelected() : null;
  }

  public static class StudentNotSelected implements ValidationError {

    @Override
    public @NotNull String getDescription() {
      return getText("ui.selectStudentDialog.studentNotSelected");
    }
  }
}
