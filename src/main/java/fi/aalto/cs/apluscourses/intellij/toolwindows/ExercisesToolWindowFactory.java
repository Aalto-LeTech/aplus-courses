package fi.aalto.cs.apluscourses.intellij.toolwindows;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ExercisesToolWindowFactory extends BaseToolWindowFactory {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ExercisesView view = new ExercisesView();

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    mainViewModel.exercisesViewModel.addValueObserver(view, ExercisesView::viewModelChanged);

    return view.getBasePanel();
  }

}
