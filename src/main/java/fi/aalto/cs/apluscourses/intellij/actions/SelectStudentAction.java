package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class SelectStudentAction extends AnAction {
  private final MainViewModelProvider mainViewModelProvider;
  private final CourseProjectProvider courseProjectProvider;

  public SelectStudentAction() {
    this(PluginSettings.getInstance(), PluginSettings.getInstance()::getCourseProject);
  }

  public SelectStudentAction(MainViewModelProvider mainViewModelProvider,
                             CourseProjectProvider courseProjectProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.courseProjectProvider = courseProjectProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var id = Messages.showInputDialog(e.getProject(), "user id", "select student", null);
    if (id == null) {
      return;
    }
    var courseProject = courseProjectProvider.getCourseProject(e.getProject());
    if (courseProject == null) {
      return;
    }
    var course = courseProject.getCourse();
    var auth = courseProject.getAuthentication();
    if (auth == null) {
      return;
    }
    var mainViewModel = mainViewModelProvider.getMainViewModel(e.getProject());
    try {
      var student = course.getExerciseDataSource().getStudent(course, auth, Long.parseLong(id));
      courseProject.selectedStudent.set(student);
      courseProject.getExercisesUpdater().restart();
      mainViewModel.exercisesViewModel.get().studentChanged(student);
      mainViewModel.exercisesViewModel.valueChanged();
      System.out.println(student == null ? "not found" : student.getFullName());
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }
}
