package fi.aalto.cs.apluscourses.intellij.toolwindows;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.ui.exercise.ExerciseGroupsView;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ExercisesToolWindowFactory extends BaseToolWindowFactory implements DumbAware {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ExerciseGroupsView view = new ExerciseGroupsView();

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    mainViewModel.exercisesViewModel.addValueObserver(view, ExerciseGroupsView::viewModelChanged);

    ObservableProperty<CourseViewModel> courseViewModelObservable
        = mainViewModel.courseViewModel;
    ObservableProperty<APlusAuthenticationViewModel> authenticationViewModelObservable
        = mainViewModel.authenticationViewModel;

    courseViewModelObservable.addValueObserver(mainViewModel.exercisesViewModel,
        (exerciseViewModelObservable, courseViewModel) -> {
          ExercisesTreeViewModel viewModel = ExercisesTreeViewModel.fromCourseAndAuthentication(
              courseViewModel, authenticationViewModelObservable.get()
          );
          exerciseViewModelObservable.set(viewModel);
        }
    );

    authenticationViewModelObservable.addValueObserver(mainViewModel.exercisesViewModel,
        (exerciseViewModelObservable, authenticationViewModel) -> {
          ExercisesTreeViewModel viewModel = ExercisesTreeViewModel.fromCourseAndAuthentication(
              courseViewModelObservable.get(), authenticationViewModel
          );
          exerciseViewModelObservable.set(viewModel);
        }
    );

    return view.getBasePanel();
  }

}
