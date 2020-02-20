package fi.aalto.cs.apluscourses.intellij.toolwindows;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.ui.ExerciseList;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ExercisesToolWindowFactory extends BaseToolWindowFactory {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    return new ExerciseList().getBasePanel();
  }
}
