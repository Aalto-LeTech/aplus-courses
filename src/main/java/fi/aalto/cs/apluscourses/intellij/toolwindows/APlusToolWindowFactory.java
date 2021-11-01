package fi.aalto.cs.apluscourses.intellij.toolwindows;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.actions.ActionGroups;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.InstallModuleAction;
import fi.aalto.cs.apluscourses.intellij.activities.InitializationActivity;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.ui.BannerView;
import fi.aalto.cs.apluscourses.ui.CollapsibleSplitter;
import fi.aalto.cs.apluscourses.ui.ProgressBarView;
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView;
import fi.aalto.cs.apluscourses.ui.module.ModulesView;
import fi.aalto.cs.apluscourses.ui.news.NewsView;
import fi.aalto.cs.apluscourses.ui.toolwindowcards.ToolWindowCardView;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class APlusToolWindowFactory extends BaseToolWindowFactory implements DumbAware {

  @Override
  protected JComponent createToolWindowContentInternal(@NotNull Project project) {
    NewsView newsView = createNewsView(project);
    ModulesView modulesView = createModulesView(project);
    ExercisesView exercisesView = createExercisesView(project);
    var collapsed = PluginSettings.getInstance().getCollapsed();
    var splitter = new CollapsibleSplitter(newsView, modulesView, exercisesView);
    splitter.collapseByTitles(collapsed);

    var cardPanel = createCardPanel(splitter.getFirstSplitter(), project);

    var progressViewModel
        = PluginSettings.getInstance().getMainViewModel(project).progressViewModel;
    var progressBarView = new ProgressBarView(progressViewModel, cardPanel).getContainer();
    return createBannerView(project, progressBarView).getContainer();
  }

  @NotNull
  private static JPanel createCardPanel(@NotNull JPanel panel, @NotNull Project project) {
    var mainViewModel = PluginSettings.getInstance().getMainViewModel(project);
    var toolWindowCardView = new ToolWindowCardView(panel, project, mainViewModel.toolWindowCardViewModel);
    InitializationActivity
        .isInitialized(project)
        .addValueObserver(mainViewModel, MainViewModel::setProjectReady);
    return toolWindowCardView;
  }

  /**
   * Creates a ModulesView.
   */
  @NotNull
  public static ModulesView createModulesView(@NotNull Project project) {
    ModulesView modulesView = new ModulesView();
    PluginSettings.getInstance().getMainViewModel(project).courseViewModel
        .addValueObserver(modulesView, ModulesView::viewModelChanged);

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

    return modulesView;
  }

  @NotNull
  private static ExercisesView createExercisesView(@NotNull Project project) {
    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);

    ExercisesView exercisesView = new ExercisesView();

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
  private static NewsView createNewsView(@NotNull Project project) {
    MainViewModel mainViewModel = PluginSettings.getInstance().getMainViewModel(project);

    InitializationActivity
        .isInitialized(project)
        .addValueObserver(mainViewModel, MainViewModel::setProjectReady);

    NewsView newsView = new NewsView();

    mainViewModel.newsTreeViewModel
        .addValueObserver(newsView, NewsView::viewModelChanged);
    ActionManager actionManager = ActionManager.getInstance();
    ActionGroup group = (ActionGroup) actionManager.getAction(ActionGroups.NEWS_ACTIONS);

    ActionToolbar toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true);
    toolbar.setTargetComponent(newsView.getNewsTree());
    newsView.toolbarContainer.add(toolbar.getComponent());

    ActionPopupMenu popupMenu =
        actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, group);
    popupMenu.setTargetComponent(newsView.getNewsTree());
    newsView.getNewsTree().setPopupMenu(popupMenu.getComponent());

    return newsView;
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
}
