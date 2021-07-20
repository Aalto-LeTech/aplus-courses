package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Student;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SelectStudentViewModel {
  @NotNull
  private final Course course;

  @NotNull
  private final Authentication authentication;

  @NotNull
  public final ObservableProperty<Student> selectedStudent = new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<Student[]> students = new ObservableReadWriteProperty<>(new Student[0]);

  /**
   * A constructor.
   */
  public SelectStudentViewModel(@NotNull List<Student> students,
                                @NotNull Course course,
                                @NotNull Authentication authentication) {
    setStudents(students);
    this.course = course;
    this.authentication = authentication;
  }

  /**
   * Sets the students and sorts them.
   */
  public void setStudents(@NotNull List<Student> newStudents) {
    students.set(newStudents
        .stream()
        .sorted(Comparator.comparing(Student::getFullName))
        .toArray(Student[]::new));
  }

  @NotNull
  public Course getCourse() {
    return course;
  }

  @NotNull
  public Authentication getAuthentication() {
    return authentication;
  }
}
