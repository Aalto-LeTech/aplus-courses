package fi.aalto.cs.apluscourses.intellij.toolwindows;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import fi.aalto.cs.apluscourses.intellij.actions.ActionGroups;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.CourseProjectAction;
import fi.aalto.cs.apluscourses.intellij.actions.InstallModuleAction;
import fi.aalto.cs.apluscourses.intellij.activities.InitializationActivity;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.ProgressBarView;
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView;
import fi.aalto.cs.apluscourses.ui.module.ModulesView;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import org.jetbrains.annotations.NotNull;

public class APlusToolWindowFactory extends BaseToolWindowFactory implements DumbAware {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ModulesView modulesView = createModulesView(project);
    ExercisesView exercisesView = createExercisesView(project);
    JBSplitter splitter = new JBSplitter(true);
    splitter.setFirstComponent(modulesView.getBasePanel());
    splitter.setSecondComponent(exercisesView.getBasePanel());

    var container = new JPanel();
    container.setLayout(new OverlayLayout(container));
    var progressBar = createProgressBarView(project).getPanel();
    progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    progressBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    splitter.setAlignmentX(Component.LEFT_ALIGNMENT);
    splitter.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    container.add(progressBar);
    container.add(splitter);

    return container;
  }

  @NotNull
  private static ModulesView createModulesView(@NotNull Project project) {
    ModulesView modulesView = new ModulesView();
    PluginSettings.getInstance().getMainViewModel(project).courseViewModel
        .addValueObserver(modulesView, ModulesView::viewModelChanged);

    InitializationActivity
        .isInitialized(project)
        .addValueObserver(modulesView, ModulesView::setProjectReady);

    ActionManager actionManager = ActionManager.getInstance();
    ActionGroup group = (ActionGroup) actionManager.getAction(ActionGroups.MODULE_ACTIONS);

    ActionToolbar toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    toolbar.setTargetComponent(modulesView.moduleListView);
    modulesView.toolbarContainer.add(toolbar.getComponent());

    ActionPopupMenu popupMenu =
        actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, group);
    popupMenu.setTargetComponent(modulesView.moduleListView);
    modulesView.moduleListView.setPopupMenu(popupMenu.getComponent());

    modulesView.moduleListView.addListActionListener(ActionUtil.createOnEventLauncher(
        InstallModuleAction.ACTION_ID, modulesView.moduleListView));
    modulesView.getEmptyText().addMouseListener(new EmptyLabelMouseAdapter());

    return modulesView;
  }

  @NotNull
  private static ExercisesView createExercisesView(@NotNull Project project) {
    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);

    InitializationActivity
        .isInitialized(project)
        .addValueObserver(mainViewModel, MainViewModel::setProjectReady);

    ExercisesView exercisesView = new ExercisesView();
    exercisesView.getEmptyTextLabel().addMouseListener(new EmptyLabelMouseAdapter());

    mainViewModel.exercisesViewModel
            .addValueObserver(exercisesView, ExercisesView::viewModelChanged);
    ActionManager actionManager = ActionManager.getInstance();
    ActionGroup group = (ActionGroup) actionManager.getAction(ActionGroups.EXERCISE_ACTIONS);

    ActionToolbar toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    toolbar.setTargetComponent(exercisesView.getExerciseGroupsTree());
    exercisesView.toolbarContainer.add(toolbar.getComponent());

    ActionPopupMenu popupMenu =
        actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, group);
    popupMenu.setTargetComponent(exercisesView.getExerciseGroupsTree());
    exercisesView.getExerciseGroupsTree().setPopupMenu(popupMenu.getComponent());

    return exercisesView;
  }

  @NotNull
  private static ProgressBarView createProgressBarView(@NotNull Project project) {
    var progressViewModel
        = PluginSettings.getInstance().getMainViewModel(project).progressViewModel;

    var progressBarView = new ProgressBarView();

    progressBarView.maxBindable.bindToSource(progressViewModel.maxValue);
    progressBarView.valueBindable.bindToSource(progressViewModel.value);
    progressBarView.labelBindable.bindToSource(progressViewModel.label);
    progressBarView.visibilityBindable.bindToSource(progressViewModel.visible);
    progressBarView.indeterminateBindable.bindToSource(progressViewModel.indeterminate);

    progressViewModel.start(2, "Loading...");

    return progressBarView;
  }

  private static class EmptyLabelMouseAdapter extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      DataContext context = DataManager.getInstance().getDataContext(e.getComponent());
      ActionUtil.launch(CourseProjectAction.ACTION_ID, context);
    }
  }
}
