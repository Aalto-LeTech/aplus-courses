package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Student;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SelectStudentViewModel {
  @NotNull
  private final List<Student> students;

  @NotNull
  public final ObservableProperty<Student> selectedStudent
      = new ObservableReadWriteProperty<>(null);

  public SelectStudentViewModel(@NotNull List<Student> students) {
    this.students = students;
  }

  @NotNull
  public List<Student> getStudents() {
    return students;
  }
}
