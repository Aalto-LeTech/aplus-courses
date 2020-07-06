package fi.aalto.cs.apluscourses.intellij.toolwindows;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.actions.ActionGroups;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class ExercisesToolWindowFactory extends BaseToolWindowFactory {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ExercisesView exercisesView = new ExercisesView();

    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    mainViewModel.exercisesViewModel.addValueObserver(exercisesView, ExercisesView::viewModelChanged);

    ActionManager actionManager = ActionManager.getInstance();
    ActionGroup group = (ActionGroup) actionManager.getAction(ActionGroups.EXERCISE_ACTIONS);

    ActionToolbar toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    toolbar.setTargetComponent(exercisesView.exerciseGroupsTree);
    exercisesView.toolbarContainer.add(toolbar.getComponent());

    return exercisesView.getBasePanel();
  }
}
