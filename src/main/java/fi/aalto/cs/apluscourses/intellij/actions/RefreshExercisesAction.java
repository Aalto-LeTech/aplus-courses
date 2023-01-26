package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.CourseProjectProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class RefreshExercisesAction extends DumbAwareAction {

  private static final Logger logger = APlusLogger.logger;

  @NotNull
  private final CourseProjectProvider courseProjectProvider;

  public RefreshExercisesAction(@NotNull CourseProjectProvider courseProjectProvider) {
    this.courseProjectProvider = courseProjectProvider;
  }

  public RefreshExercisesAction() {
    this(PluginSettings.getInstance()::getCourseProject);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var project = e.getProject();
    var courseProject = courseProjectProvider.getCourseProject(project);
    e.getPresentation().setEnabled(
        project != null && courseProject != null && courseProject.getAuthentication() != null);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var courseProject = courseProjectProvider.getCourseProject(e.getProject());
    if (courseProject == null) {
      return;
    }
    if (e.getInputEvent().isShiftDown()) {
      logger.info("Requested manual cache purge");
      courseProject.getCourse().getExerciseDataSource().clearCache();
    }
    courseProject.getExercisesUpdater().restart();
  }
}
