package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import java.awt.Desktop;
import java.net.URI;
import org.jetbrains.annotations.NotNull;

public class OpenSubmissionAction extends DumbAwareAction {

  public static final String ACTION_ID = OpenSubmissionAction.class.getCanonicalName();

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    ExercisesTreeViewModel exercisesTree = PluginSettings
        .getInstance()
        .getMainViewModel(e.getProject())
        .exercisesViewModel
        .get();
    if (exercisesTree == null) {
      return;
    }

    SubmissionResultViewModel submission = exercisesTree.getSelectedSubmission();
    if (submission == null) {
      return;
    }

    try {
      Desktop desktop = Desktop.getDesktop();
      desktop.browse(new URI(submission.getSubmissionUrl()));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }
}
