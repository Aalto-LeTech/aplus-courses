package fi.aalto.cs.apluscourses.toolwindows

import com.intellij.openapi.Disposable
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
import fi.aalto.cs.apluscourses.model.news.NewsTree
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.presentation.CourseViewModel
import fi.aalto.cs.apluscourses.services.PluginSettings
import fi.aalto.cs.apluscourses.services.course.CourseUpdaterService
import fi.aalto.cs.apluscourses.services.course.CourseUpdaterService.NewsUpdaterListener
import fi.aalto.cs.apluscourses.services.exercise.ExercisesTreeFilterService
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService.ExercisesUpdaterListener
import fi.aalto.cs.apluscourses.ui.BannerView
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView
import fi.aalto.cs.apluscourses.ui.module.ModulesView
import fi.aalto.cs.apluscourses.ui.news.NewsView
import fi.aalto.cs.apluscourses.ui.overview.OverviewView
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent

internal class APlusToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Hide the title of the tool window, which is replaced by the overview tab
        toolWindow.component.putClientProperty(
            ToolWindowContentUi.HIDE_ID_LABEL, "true"
        )

        val newsView = createNewsView(project, toolWindow)
        val modulesView = createModulesView(project)
        val exercisesView = createExercisesView(project, toolWindow.disposable)

        val contentFactory = ContentFactory.getInstance()

        val overviewTab = contentFactory.createContent(
            OverviewView(project),
            "<html><body><b>A+ Courses</b></body></html>",
            true
        )
        val exercisesTab = contentFactory.createContent(
            exercisesView,
            "Assignments",
            true
        )
        val modulesTab = contentFactory.createContent(
            modulesView,
            "Modules",
            true
        )
        val newsTab = contentFactory.createContent(
            newsView,
            "News",
            true
        )

        toolWindow.contentManager.addContent(overviewTab)
        toolWindow.contentManager.addContent(exercisesTab)
        toolWindow.contentManager.addContent(modulesTab)
        toolWindow.contentManager.addContent(newsTab)
        toolWindow.contentManager.setSelectedContent(overviewTab)

        // Shorten titles when tool window is too small
        toolWindow.component.addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent?) {
                if (e != null) {
                    println(e.component.bounds.width)
                    if (e.component.bounds.width <= 460) {
                        overviewTab.displayName = "<html><body><b>A+</b></body></html>"
                    } else {
                        overviewTab.displayName = "<html><body><b>A+ Courses</b></body></html>"
                    }
                    if (e.component.bounds.width <= 390) {
                        newsView.setShortTab(true)
                        modulesTab.displayName = "📦"
                        exercisesTab.displayName = "📚"
                    } else {
                        newsView.setShortTab(false)
                        modulesTab.displayName = "Modules"
                        exercisesTab.displayName = "Assignments"
                    }
                }
            }

            override fun componentMoved(e: ComponentEvent?) {}
            override fun componentShown(e: ComponentEvent?) {}
            override fun componentHidden(e: ComponentEvent?) {}
        })


        // Add shortcut to settings to the title bar
        val showSettingsAction = ShowSettingsAction()
        toolWindow.setTitleActions(listOf(showSettingsAction))

        // Hide the title of the tool window, which is replaced by the overview tab
        toolWindow.component.putClientProperty(
            ToolWindowContentUi.HIDE_ID_LABEL, "true"
        )

        project.service<CourseUpdaterService>().restart()
    }
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
    val modulesView = ModulesView(project)
    PluginSettings.getInstance().getMainViewModel(project).courseViewModel
        .addValueObserver(modulesView) { obj: ModulesView, course: CourseViewModel? ->
            obj.viewModelChanged(course)
            if (course != null) {
                println(course.modules)
            }
        }

//    val actionManager = ActionManager.getInstance()
//    val group = actionManager.getAction(ActionGroups.MODULE_ACTIONS) as ActionGroup

//    val toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true)
//    toolbar.targetComponent = modulesView.list
//    modulesView.toolbar = toolbar.component

//    val popupMenu =
//        actionManager.createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, group)
//    popupMenu.setTargetComponent(modulesView.moduleListView)
//    modulesView.moduleListView.setPopupMenu(popupMenu.component)
//
//    modulesView.moduleListView.addListActionListener(
//        ActionUtil.createOnEventLauncher(
//            InstallModuleAction.ACTION_ID, modulesView.moduleListView
//        )
//    )

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

//    Disposer.register(disposable) {
//        project.service<ExercisesUpdaterService>().stop()
//        connection.dispose()
//        println("disposed")
//    }

    connection.subscribe(ExercisesUpdaterService.EXERCISES_TOPIC, object : ExercisesUpdaterListener {
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
//    val mainViewModel = PluginSettings.getInstance().getMainViewModel(project)

//    isInitialized(project)
//        .addValueObserver(mainViewModel) { obj: MainViewModel, isReady: Boolean? ->
//            obj.setProjectReady(
//                isReady ?: return@addValueObserver
//            )
//        }

    val newsView = NewsView(toolWindow, project)

    val connection = project.messageBus.connect(toolWindow.disposable)

    connection.subscribe(CourseUpdaterService.NEWS_TOPIC, object : NewsUpdaterListener {
        override fun onNewsUpdated(newsTree: NewsTree) {
            println("received update news mytoolwindow")
            newsView.viewModelChanged(newsTree)
        }
    })

    Disposer.register(toolWindow.disposable) {
        project.service<CourseUpdaterService>().stop()
        connection.dispose()
        println("disposed")
    }

//    mainViewModel.newsTreeViewModel
//        .addValueObserver(newsView) { obj: NewsView, viewModel: NewsTreeViewModel? ->
//            obj.viewModelChanged(
//                viewModel
//            )
//        }
//    val actionManager = ActionManager.getInstance()
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

//    val mainViewModel = PluginSettings.getInstance()
//        .getMainViewModel(project)
//    mainViewModel.bannerViewModel
//        .addValueObserver(bannerView) { obj: BannerView, viewModel: BannerViewModel? ->
//            obj.viewModelChanged(
//                viewModel
//            )
//        }

    return bannerView
}