package fi.aalto.cs.apluscourses.toolwindows

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import fi.aalto.cs.apluscourses.actions.ActionGroups.EXERCISE_ACTIONS
import fi.aalto.cs.apluscourses.actions.ShowSettingsAction
import fi.aalto.cs.apluscourses.activities.isInitialized
import fi.aalto.cs.apluscourses.intellij.actions.ActionGroups
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil
import fi.aalto.cs.apluscourses.intellij.actions.InstallModuleAction
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.presentation.BannerViewModel
import fi.aalto.cs.apluscourses.presentation.CourseViewModel
import fi.aalto.cs.apluscourses.presentation.MainViewModel
import fi.aalto.cs.apluscourses.presentation.news.NewsTreeViewModel
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.exercise.ExercisesTreeFilterService
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService.ExercisesUpdaterListener
import fi.aalto.cs.apluscourses.ui.BannerView
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView
import fi.aalto.cs.apluscourses.ui.module.ModulesView
import fi.aalto.cs.apluscourses.ui.news.NewsView
import fi.aalto.cs.apluscourses.ui.overview.OverviewView
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent

internal class APlusToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
//        val component = createToolWindowContentInternal(project)
        val newsView = createNewsView(project, toolWindow)
        val modulesView = createModulesView(project)
        val exercisesView = createExercisesView(project, toolWindow.disposable)
        val contentFactory = ContentFactory.getInstance()
//        contentFactory.createContentManager(TabbedPaneContentUI(), true, project)
        val content0 =
            contentFactory.createContent(OverviewView(project), "<html><body><b>A+ Courses</b></body></html>", true)
        val content = contentFactory.createContent(
            newsView,
            "News",
            true
        )
        val content2 = contentFactory.createContent(modulesView, "Modules", true)
        val content3 = contentFactory.createContent(exercisesView, "Assignments", true)

//        val content4 = contentFactory.createContent(exercisesView.toolbarContainer, "Settings", true)
        toolWindow.contentManager.addContent(content0)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.addContent(content2)
        toolWindow.contentManager.addContent(content3)

//        toolWindow.contentManager.addContent(content4)


        val showSettingsAction = ShowSettingsAction()
        toolWindow.setTitleActions(listOf(showSettingsAction))

        toolWindow.component.putClientProperty(
            ToolWindowContentUi.HIDE_ID_LABEL, "true"
        )
    }

//    private fun createToolWindowContentInternal(project: Project): JComponent {
//        val newsView = createNewsView(project)
//        val modulesView = createModulesView(project)
//        val exercisesView = createExercisesView(project)
//        val collapsed = PluginSettings.getInstance().collapsed
////        val splitter = CollapsibleSplitter(newsView, modulesView, exercisesView)
//        splitter.collapseByTitles(collapsed)
//
//        val cardPanel = createCardPanel(splitter.firstSplitter, project)
//
//        val progressViewModel = PluginSettings.getInstance().getMainViewModel(project).progressViewModel
//        val progressBarView = ProgressBarView(progressViewModel, cardPanel).container
//        return createBannerView(project, progressBarView).container
//    }
}

//private fun createCardPanel(panel: JPanel, project: Project): JPanel {
//    val mainViewModel = PluginSettings.getInstance().getMainViewModel(project)
//    val toolWindowCardView = ToolWindowCardView(panel, project, mainViewModel.toolWindowCardViewModel)
//    isInitialized(project)
//        .addValueObserver(mainViewModel) { obj: MainViewModel, isReady: Boolean? ->
//            obj.setProjectReady(
//                isReady ?: return@addValueObserver
//            )
//        }
//    return toolWindowCardView
//}

/**
 * Creates a ModulesView.
 */
fun createModulesView(project: Project): ModulesView {
    val modulesView = ModulesView()
    PluginSettings.getInstance().getMainViewModel(project).courseViewModel
        .addValueObserver(modulesView) { obj: ModulesView, course: CourseViewModel? ->
            obj.viewModelChanged(
                course
            )
        }

    val actionManager = ActionManager.getInstance()
    val group = actionManager.getAction(ActionGroups.MODULE_ACTIONS) as ActionGroup

    val toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true)
    toolbar.targetComponent = modulesView.moduleListView
    modulesView.toolbar = toolbar.component

    val popupMenu =
        actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, group)
    popupMenu.setTargetComponent(modulesView.moduleListView)
    modulesView.moduleListView.setPopupMenu(popupMenu.component)

    modulesView.moduleListView.addListActionListener(
        ActionUtil.createOnEventLauncher(
            InstallModuleAction.ACTION_ID, modulesView.moduleListView
        )
    )

    return modulesView
}

private fun createExercisesView(project: Project, disposable: Disposable): ExercisesView {
    val exercisesView = ExercisesView(project)

    val toolbar = ActionManager.getInstance().createActionToolbar(
        ActionPlaces.TOOLBAR,
        EXERCISE_ACTIONS.get(),
        true
    )
    toolbar.targetComponent = exercisesView.exerciseGroupsFilteringTree.component
    val customToolbar = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(exercisesView.searchTextField)
        add(Box.createHorizontalGlue())
        add(toolbar.component)
    }
    exercisesView.toolbar = customToolbar
//    project.service<ExercisesUpdaterService>().restart()

    val connection = project.messageBus.connect(disposable)

    Disposer.register(disposable) {
        project.service<ExercisesUpdaterService>().stop()
        connection.dispose()
        println("disposed")
    }

    connection.subscribe(ExercisesUpdaterService.TOPIC, object : ExercisesUpdaterListener {
        override fun onExercisesUpdated() {
            println("received update mytoolwindow")
            exercisesView.updateTree()
        }

        override fun onExerciseUpdated(exercise: Exercise) {
            exercisesView.updateExercise(exercise)
        }
    })

    connection.subscribe(
        ExercisesTreeFilterService.TOPIC,
        object : ExercisesTreeFilterService.ExercisesTreeFilterListener {
            override fun onFilterUpdated() {
                exercisesView.updateTree()
            }
        })

    return exercisesView
}

private fun createNewsView(project: Project, toolWindow: ToolWindow): NewsView {
    val mainViewModel = PluginSettings.getInstance().getMainViewModel(project)

    isInitialized(project)
        .addValueObserver(mainViewModel) { obj: MainViewModel, isReady: Boolean? ->
            obj.setProjectReady(
                isReady ?: return@addValueObserver
            )
        }

    val newsView = NewsView(toolWindow)

    mainViewModel.newsTreeViewModel
        .addValueObserver(newsView) { obj: NewsView, viewModel: NewsTreeViewModel? ->
            obj.viewModelChanged(
                viewModel
            )
        }
    val actionManager = ActionManager.getInstance()
//    val group = actionManager.getAction(ActionGroups.NEWS_ACTIONS) as ActionGroup

//    val toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true)
//    toolbar.targetComponent = newsView.newsTree
//    newsView.toolbar = toolbar.component
//
//    val popupMenu =
//        actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, group)
//    popupMenu.setTargetComponent(newsView.newsTree)
//    newsView.newsTree.setPopupMenu(popupMenu.component)

    return newsView
}

private fun createBannerView(
    project: Project,
    bottomComponent: JComponent
): BannerView {
    val bannerView = BannerView(bottomComponent)

    val mainViewModel = PluginSettings.getInstance()
        .getMainViewModel(project)
    mainViewModel.bannerViewModel
        .addValueObserver(bannerView) { obj: BannerView, viewModel: BannerViewModel? ->
            obj.viewModelChanged(
                viewModel
            )
        }

    return bannerView
}