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
import fi.aalto.cs.apluscourses.intellij.actions.ActionGroups;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.CourseProjectAction;
import fi.aalto.cs.apluscourses.intellij.actions.InstallModuleAction;
import fi.aalto.cs.apluscourses.intellij.activities.InitializationActivity;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.BannerView;
import fi.aalto.cs.apluscourses.ui.CollapsibleSplitter;
import fi.aalto.cs.apluscourses.ui.ProgressBarView;
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView;
import fi.aalto.cs.apluscourses.ui.module.ModulesView;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class APlusToolWindowFactory extends BaseToolWindowFactory implements DumbAware {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    ModulesView modulesView = createModulesView(project);
    // TODO remove, for demo purposes
    ModulesView modulesView2 = createModulesView(project);
    ExercisesView exercisesView = createExercisesView(project);
    var collapsed = PluginSettings.getInstance().getCollapsed();
    var splitter = new CollapsibleSplitter(modulesView, modulesView2, exercisesView);
    splitter.collapseByTitles(collapsed);

    var progressViewModel
        = PluginSettings.getInstance().getMainViewModel(project).progressViewModel;
    var progressBarView = new ProgressBarView(progressViewModel, splitter.getFirstSplitter()).getContainer();
    return createBannerView(project, progressBarView).getContainer();
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
  private static BannerView createBannerView(@NotNull Project project,
                                             @NotNull JComponent bottomComponent) {
    var bannerView = new BannerView(bottomComponent);

    PluginSettings.getInstance()
            .getMainViewModel(project)
            .bannerViewModel
            .addValueObserver(bannerView, BannerView::viewModelChanged);

    return bannerView;
  }

  private static class EmptyLabelMouseAdapter extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      DataContext context = DataManager.getInstance().getDataContext(e.getComponent());
      ActionUtil.launch(CourseProjectAction.ACTION_ID, context);
    }
  }
}
