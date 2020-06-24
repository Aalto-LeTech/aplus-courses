package fi.aalto.cs.apluscourses.intellij.toolwindows;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.exercise.ExerciseGroupsView;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ExercisesToolWindowFactory extends BaseToolWindowFactory implements DumbAware {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ExerciseGroupsView view = new ExerciseGroupsView();

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    mainViewModel.exercisesViewModel.addValueObserver(view, ExerciseGroupsView::viewModelChanged);

    return view.getBasePanel();
  }

}
