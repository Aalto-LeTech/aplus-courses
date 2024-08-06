package fi.aalto.cs.apluscourses.toolwindows

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
import com.intellij.ui.content.*
import com.intellij.util.messages.MessageBusConnection
import fi.aalto.cs.apluscourses.actions.ActionGroups.EXERCISE_ACTIONS
import fi.aalto.cs.apluscourses.actions.ActionGroups.TOOL_WINDOW_ACTIONS
import fi.aalto.cs.apluscourses.actions.RefreshEverythingAction
import fi.aalto.cs.apluscourses.dal.TokenStorage
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.news.NewsTree
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.CourseManager.NewsUpdaterListener
import fi.aalto.cs.apluscourses.services.exercise.ExercisesTreeFilterService
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService.ExercisesUpdaterListener
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView
import fi.aalto.cs.apluscourses.ui.module.ModulesView
import fi.aalto.cs.apluscourses.ui.news.NewsView
import fi.aalto.cs.apluscourses.ui.overview.OverviewView
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.Box
import javax.swing.BoxLayout

internal class APlusToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Hide the title of the tool window, which is replaced by the overview tab
        toolWindow.component.putClientProperty(
            ToolWindowContentUi.HIDE_ID_LABEL, "true"
        )
        val connection = project.messageBus.connect(toolWindow.disposable)
        val overviewView = createOverviewView(project, connection)
        val newsView = createNewsView(project, toolWindow)
        val modulesView = createModulesView(project, connection)
        val exercisesView = createExercisesView(project, toolWindow, connection)
        project.service<ExercisesUpdaterService>().restart() // TODO
        project.service<CourseManager>().restart()

        val contentFactory = ContentFactory.getInstance()

        val overviewTab = contentFactory.createContent(
            overviewView,
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

        createConnections(overviewView, newsView, modulesView, exercisesView, modulesTab, exercisesTab, toolWindow)


        toolWindow.contentManager.addContent(overviewTab)
        toolWindow.contentManager.addContent(exercisesTab)
        toolWindow.contentManager.addContent(modulesTab)
        toolWindow.contentManager.addContent(newsTab)
        toolWindow.contentManager.setSelectedContent(overviewTab)

        toolWindow.setAdditionalGearActions(
            TOOL_WINDOW_ACTIONS.get()
        )

        // Shorten titles when toolwindow is too small
        toolWindow.component.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                println("toolwindow resize ${e.component.bounds.width}")
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
        })

        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            private var previousSelection: Content? = null
            override fun selectionChanged(event: ContentManagerEvent) {
                if (previousSelection == newsTab) {
                    println("previous was news")
                    project.service<CourseManager>().setNewsAsRead()
                }
                previousSelection = event.content
            }
        })

        // Hide the title of the tool window, which is replaced by the overview tab
        toolWindow.component.putClientProperty(
            ToolWindowContentUi.HIDE_ID_LABEL, "true"
        )

        // Force resize to trigger componentResized
        toolWindow.component.size = Dimension(toolWindow.component.width - 1, toolWindow.component.height)
    }
}

fun createConnections(
    overviewView: OverviewView,
    newsView: NewsView,
    modulesView: ModulesView,
    exercisesView: ExercisesView,
    modulesContent: Content,
    exercisesContent: Content,
    toolWindow: ToolWindow
) {
    val connection = toolWindow.project.messageBus.connect(toolWindow.disposable)

    connection.subscribe(CourseManager.COURSE_TOPIC, object : CourseManager.CourseListener {
        override fun onCourseUpdated(course: Course?) {
            overviewView.update()
        }
    })

    connection.subscribe(ExercisesUpdaterService.EXERCISES_TOPIC, object : ExercisesUpdaterListener {
        override fun onExercisesUpdated() {
            exercisesView.updateTree()
            modulesView.viewModelChanged(CourseManager.course(toolWindow.project))
            overviewView.update()
        }

        override fun onExerciseUpdated(exercise: Exercise) {
            exercisesView.updateExercise(exercise)
        }

        override fun onPointsByDifficultyUpdated(pointsByDifficulty: Map<String, Int>?) {
            overviewView.update()
        }
    })

    connection.subscribe(
        ExercisesTreeFilterService.TOPIC,
        object : ExercisesTreeFilterService.ExercisesTreeFilterListener {
            override fun onFilterUpdated() {
                exercisesView.updateTree()
            }
        })

    connection.subscribe(Opener.SHOW_ITEM_TOPIC, object : Opener.ItemOpenerListener {
        override fun onExerciseOpened(exercise: Exercise) {
            exercisesView.showExercise(exercise)
            toolWindow.contentManager.setSelectedContent(exercisesContent)
        }

        override fun onModuleOpened(module: Module) {
            modulesView.showModule(module, true)
            toolWindow.contentManager.setSelectedContent(modulesContent)
        }
    })

    connection.subscribe(CourseManager.NEWS_TOPIC, object : NewsUpdaterListener {
        override fun onNewsUpdated(newsTree: NewsTree) {
            println("received update news mytoolwindow")
            newsView.viewModelChanged(newsTree)
        }
    })
    connection.subscribe(CourseManager.MODULES_TOPIC, object : CourseManager.ModuleListener {
        override fun onModulesUpdated(course: Course?) {
            modulesView.viewModelChanged(course)
        }
    })
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

fun createOverviewView(project: Project, connection: MessageBusConnection): OverviewView {
    val overviewView = OverviewView(project)
    return overviewView
}

/**
 * Creates a ModulesView.
 */
fun createModulesView(project: Project, connection: MessageBusConnection): ModulesView {
    val modulesView = ModulesView(project)
//    PluginSettings.getInstance().getMainViewModel(project).courseViewModel
//        .addValueObserver(modulesView) { obj: ModulesView, course: CourseViewModel? ->
//            obj.viewModelChanged(course)
//            if (course != null) {
//                println(course.modules)
//            }
//        }
//    connection.subscribe(ModuleInstaller.MODULE_INSTALLED_TOPIC, object : ModuleInstaller.ModuleInstallerListener {
////        override fun onModuleInstalled(module: Module) {
//////            modulesView.viewModelChanged(CourseManager.course(project))
////            modulesView.viewModelChanged(PluginSettings.getInstance().getMainViewModel(project).courseViewModel.get())
////        }
////    })
    val customToolbar = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(modulesView.searchTextField)
//        add(Box.createHorizontalGlue())
//        add(toolbar.component)
    }
    modulesView.toolbar = customToolbar

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

private fun createExercisesView(
    project: Project,
    toolWindow: ToolWindow,
    connection: MessageBusConnection
): ExercisesView {
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

//    Disposer.register(disposable) {
//        project.service<ExercisesUpdaterService>().stop()
//        connection.dispose()
//        println("disposed")
//    }
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

    Disposer.register(toolWindow.disposable) {
        project.service<CourseManager>().stop()
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

//private fun createBannerView(
//    project: Project,
//    bottomComponent: JComponent
//): BannerView {
//    val bannerView = BannerView(bottomComponent)
//
////    val mainViewModel = PluginSettings.getInstance()
////        .getMainViewModel(project)
////    mainViewModel.bannerViewModel
////        .addValueObserver(bannerView) { obj: BannerView, viewModel: BannerViewModel? ->
////            obj.viewModelChanged(
////                viewModel
////            )
////        }
//
//    return bannerView
//}