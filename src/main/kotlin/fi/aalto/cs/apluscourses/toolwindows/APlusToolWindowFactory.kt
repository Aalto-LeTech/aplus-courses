package fi.aalto.cs.apluscourses.toolwindows

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.actions.ActionGroups.EXERCISE_ACTIONS
import fi.aalto.cs.apluscourses.actions.ActionGroups.MODULE_ACTIONS
import fi.aalto.cs.apluscourses.actions.ActionGroups.TOOL_WINDOW_ACTIONS
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.news.NewsList
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.CourseManager.NewsUpdaterListener
import fi.aalto.cs.apluscourses.services.course.InitializationStatus
import fi.aalto.cs.apluscourses.services.exercise.ExercisesTreeFilter
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater.ExercisesUpdaterListener
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
        toolWindow.isShowStripeButton = true
        val connection = project.messageBus.connect(toolWindow.disposable)
        val overviewView = createOverviewView(project)
        val newsView = createNewsView(toolWindow)
        val modulesView = createModulesView(project)
        val exercisesView = createExercisesView(project)

        val contentFactory = ContentFactory.getInstance()

        val overviewTab = contentFactory.createContent(
            overviewView,
            message("toolwindows.APlusToolWindowFactory.tabs.overview"),
            true
        )
        val exercisesTab = contentFactory.createContent(
            exercisesView,
            message("toolwindows.APlusToolWindowFactory.tabs.exercises"),
            true
        )
        val modulesTab = contentFactory.createContent(
            modulesView,
            message("toolwindows.APlusToolWindowFactory.tabs.modules"),
            true
        )
        val newsTab = contentFactory.createContent(
            newsView,
            message("toolwindows.APlusToolWindowFactory.tabs.news"),
            true
        )

        fun addAllTabs() {
            toolWindow.contentManager.addContent(exercisesTab)
            toolWindow.contentManager.addContent(modulesTab)
            toolWindow.contentManager.addContent(newsTab)
        }

        fun removeAllTabs() {
            toolWindow.contentManager.removeContent(exercisesTab, true)
            toolWindow.contentManager.removeContent(modulesTab, true)
            toolWindow.contentManager.removeContent(newsTab, true)
        }

        toolWindow.contentManager.addContent(overviewTab)
        toolWindow.contentManager.addContent(exercisesTab)
        toolWindow.contentManager.addContent(modulesTab)
        toolWindow.contentManager.addContent(newsTab)
        toolWindow.contentManager.setSelectedContent(overviewTab)

        toolWindow.setAdditionalGearActions(TOOL_WINDOW_ACTIONS)

        // Shorten titles when toolwindow is too small
        toolWindow.component.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                if (e.component.bounds.width <= 424) {
                    overviewTab.displayName = message("toolwindows.APlusToolWindowFactory.tabs.overviewShort")
                } else {
                    overviewTab.displayName = message("toolwindows.APlusToolWindowFactory.tabs.overview")
                }
                if (e.component.bounds.width <= 370) {
                    newsView.setShortTab(true)
                    modulesTab.displayName = message("toolwindows.APlusToolWindowFactory.tabs.modulesShort")
                    exercisesTab.displayName = message("toolwindows.APlusToolWindowFactory.tabs.exercisesShort")
                } else {
                    newsView.setShortTab(false)
                    modulesTab.displayName = message("toolwindows.APlusToolWindowFactory.tabs.modules")
                    exercisesTab.displayName = message("toolwindows.APlusToolWindowFactory.tabs.exercises")
                }
            }
        })

        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            private var previousSelection: Content? = null
            override fun selectionChanged(event: ContentManagerEvent) {
                if (previousSelection == newsTab) {
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

        connection.subscribe(CourseManager.COURSE_TOPIC, object : CourseManager.CourseListener {
            override fun onCourseUpdated(course: Course?) {
                overviewView.update()
                if (course == null) removeAllTabs() else addAllTabs()
            }
        })

        connection.subscribe(ExercisesUpdater.EXERCISES_TOPIC, object : ExercisesUpdaterListener {
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
            ExercisesTreeFilter.TOPIC,
            object : ExercisesTreeFilter.ExercisesTreeFilterListener {
                override fun onFilterUpdated() {
                    exercisesView.updateTree()
                }
            })

        connection.subscribe(Opener.SHOW_ITEM_TOPIC, object : Opener.ItemOpenerListener {
            override fun onExerciseOpened(exercise: Exercise) {
                exercisesView.showExercise(exercise)
                toolWindow.contentManager.setSelectedContent(exercisesTab)
            }

            override fun onModuleOpened(module: Module) {
                modulesView.showModule(module, true)
                toolWindow.contentManager.setSelectedContent(modulesTab)
            }
        })

        connection.subscribe(CourseManager.NEWS_TOPIC, object : NewsUpdaterListener {
            override fun onNewsUpdated(newsList: NewsList) {
                newsView.viewModelChanged(newsList)
            }
        })
        connection.subscribe(CourseManager.MODULES_TOPIC, object : CourseManager.ModuleListener {
            override fun onModulesUpdated(course: Course?) {
                modulesView.viewModelChanged(course)
            }
        })

        if (InitializationStatus.isNotCourse(project) || InitializationStatus.isIoError(project)) {
            removeAllTabs()
        }
    }

    private fun createOverviewView(project: Project): OverviewView = OverviewView(project)

    private fun createModulesView(project: Project): ModulesView {
        val modulesView = ModulesView(project)

        val toolbar = ActionManager.getInstance().createActionToolbar(
            ActionPlaces.TOOLBAR,
            MODULE_ACTIONS,
            true
        )
        toolbar.targetComponent = modulesView.component
        modulesView.toolbar = createCustomToolbar(modulesView, toolbar)
        return modulesView
    }

    private fun createExercisesView(
        project: Project
    ): ExercisesView {
        val exercisesView = ExercisesView(project)

        val toolbar = ActionManager.getInstance().createActionToolbar(
            ActionPlaces.TOOLBAR,
            EXERCISE_ACTIONS,
            true
        )
        toolbar.targetComponent = exercisesView.exerciseGroupsFilteringTree.component
        exercisesView.toolbar = createCustomToolbar(exercisesView, toolbar)

        project.service<ExercisesTreeFilter>().loadFromState()

        return exercisesView
    }

    private fun createCustomToolbar(tab: SearchableTab, toolbar: ActionToolbar) =
        JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(tab.searchTextField)
            add(Box.createHorizontalGlue())
            add(toolbar.component)
        }


    private fun createNewsView(toolWindow: ToolWindow): NewsView = NewsView(toolWindow)

}