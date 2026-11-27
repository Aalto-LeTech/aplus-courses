package fi.aalto.cs.apluscourses.toolwindows

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
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
import com.intellij.util.application
import com.intellij.util.messages.MessageBusConnection
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.actions.ActionGroups.EXERCISE_ACTIONS
import fi.aalto.cs.apluscourses.actions.ActionGroups.MODULE_ACTIONS
import fi.aalto.cs.apluscourses.actions.ActionGroups.TOOL_WINDOW_ACTIONS
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.model.Course
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.course.CourseNews
import fi.aalto.cs.apluscourses.services.course.CourseSetupStatus
import fi.aalto.cs.apluscourses.services.course.InitializationStatus
import fi.aalto.cs.apluscourses.services.exercise.ExercisesTreeFilter
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdater.ExercisesUpdaterListener
import fi.aalto.cs.apluscourses.ui.exercise.ExercisesView
import fi.aalto.cs.apluscourses.ui.module.ModulesView
import fi.aalto.cs.apluscourses.ui.overview.OverviewView
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import org.jetbrains.annotations.NonNls
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.Box
import javax.swing.BoxLayout

internal class APlusToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // The overview tab supplies the tool window title.
        toolWindow.component.putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true")
        toolWindow.isShowStripeButton = true
        toolWindow.setAdditionalGearActions(TOOL_WINDOW_ACTIONS)

        val overviewView = createOverviewView(project)
        val modulesView = createModulesView(project)
        val exercisesView = createExercisesView(project)
        val tabs = addTabs(toolWindow, overviewView, exercisesView, modulesView)

        updateCourseTabNews(tabs.overview, CourseNews.getInstance(project).hasUnread())
        keepTitlesFitting(toolWindow, tabs)
        markNewsSeenOnLeavingOverview(project, toolWindow, tabs.overview)

        val connection = project.messageBus.connect(toolWindow.disposable)
        subscribeToCourse(connection, project, tabs, overviewView, modulesView)
        subscribeToExercises(connection, project, overviewView, exercisesView, modulesView)
        subscribeToOpening(connection, toolWindow, tabs, overviewView, exercisesView, modulesView)

        if (InitializationStatus.isNotCourse(project) || InitializationStatus.isIoError(project)) {
            tabs.hideCourseTabs()
        }
        closeAiAssistantAd(project)
    }

    private class Tabs(
        private val toolWindow: ToolWindow,
        val overview: Content,
        val exercises: Content,
        val modules: Content,

        val titled: List<Pair<Content, String>>
    ) {
        fun showCourseTabs() {
            if (toolWindow.contentManager.getIndexOfContent(exercises) < 0) {
                toolWindow.contentManager.addContent(exercises)
            }
            if (toolWindow.contentManager.getIndexOfContent(modules) < 0) {
                toolWindow.contentManager.addContent(modules)
            }
        }

        fun hideCourseTabs() {
            toolWindow.contentManager.removeContent(exercises, false)
            toolWindow.contentManager.removeContent(modules, false)
        }
    }

    private fun addTabs(
        toolWindow: ToolWindow,
        overviewView: OverviewView,
        exercisesView: ExercisesView,
        modulesView: ModulesView
    ): Tabs {
        val contentFactory = ContentFactory.getInstance()
        val overviewTitle = message("toolwindows.APlusToolWindowFactory.tabs.overview")
        val exercisesTitle = message("toolwindows.APlusToolWindowFactory.tabs.exercises")
        val modulesTitle = message("toolwindows.APlusToolWindowFactory.tabs.modules")

        val overviewTab = contentFactory.createContent(overviewView, overviewTitle, true)
        val exercisesTab = contentFactory.createContent(exercisesView, exercisesTitle, true)
        val modulesTab = contentFactory.createContent(modulesView, modulesTitle, true)

        overviewTab.icon = CoursesIcons.LogoColor
        exercisesTab.icon = CoursesIcons.ExerciseGroup
        modulesTab.icon = CoursesIcons.Module

        toolWindow.contentManager.addContent(overviewTab)
        toolWindow.contentManager.addContent(exercisesTab)
        toolWindow.contentManager.addContent(modulesTab)
        toolWindow.contentManager.setSelectedContent(overviewTab)

        return Tabs(
            toolWindow,
            overviewTab,
            exercisesTab,
            modulesTab,
            listOf(
                overviewTab to overviewTitle,
                exercisesTab to exercisesTitle,
                modulesTab to modulesTitle
            )
        )
    }

    private fun keepTitlesFitting(toolWindow: ToolWindow, tabs: Tabs) {
        val component = toolWindow.component
        val titleSwitch = TabTitleSwitch { show -> showTabTitles(tabs.titled, show) }
        component.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                titleSwitch.resized(component.width)
            }
        })
        titleSwitch.resized(component.width)
    }

    private fun markNewsSeenOnLeavingOverview(
        project: Project,
        toolWindow: ToolWindow,
        overviewTab: Content
    ) {
        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun selectionChanged(event: ContentManagerEvent) {
                if (!leavesOverview(event, overviewTab)) return
                if (project.isDisposed || toolWindow.contentManager.isDisposed) return
                val news = CourseNews.getInstance(project)
                if (news.hasUnread()) news.setAllRead()
            }
        })
    }

    private fun subscribeToCourse(
        connection: MessageBusConnection,
        project: Project,
        tabs: Tabs,
        overviewView: OverviewView,
        modulesView: ModulesView
    ) {
        connection.subscribe(CourseSetupStatus.TOPIC, CourseSetupStatus.SetupListener {
            overviewView.update()
        })

        connection.subscribe(CourseManager.COURSE_TOPIC, object : CourseManager.CourseListener {
            override fun onCourseUpdated(course: Course?) {
                overviewView.update()
                updateCourseTabNews(tabs.overview, CourseNews.getInstance(project).hasUnread())
                if (course == null) tabs.hideCourseTabs() else tabs.showCourseTabs()
            }
        })

        connection.subscribe(CourseManager.MODULES_TOPIC, object : CourseManager.ModuleListener {
            override fun onModulesUpdated(course: Course?) {
                modulesView.viewModelChanged(course)
            }

            override fun onModuleStatusChanged(module: Module) {
                modulesView.updateModuleIcon(module)
            }
        })

        connection.subscribe(CourseNews.TOPIC, object : CourseNews.Listener {
            override fun onNewsUpdated() {
                updateCourseTabNews(tabs.overview, CourseNews.getInstance(project).hasUnread())
                overviewView.update()
            }
        })
    }

    private fun subscribeToExercises(
        connection: MessageBusConnection,
        project: Project,
        overviewView: OverviewView,
        exercisesView: ExercisesView,
        modulesView: ModulesView
    ) {
        connection.subscribe(ExercisesUpdater.EXERCISES_TOPIC, object : ExercisesUpdaterListener {
            override fun onExercisesUpdated() {
                exercisesView.updateTree()
                modulesView.viewModelChanged(CourseManager.course(project))
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
    }

    private fun subscribeToOpening(
        connection: MessageBusConnection,
        toolWindow: ToolWindow,
        tabs: Tabs,
        overviewView: OverviewView,
        exercisesView: ExercisesView,
        modulesView: ModulesView
    ) {
        connection.subscribe(Opener.SHOW_ITEM_TOPIC, object : Opener.ItemOpenerListener {
            override fun onExerciseOpened(exercise: Exercise) {
                exercisesView.showExercise(exercise)
                toolWindow.contentManager.setSelectedContent(tabs.exercises)
            }

            override fun onModuleOpened(module: Module) {
                modulesView.showModule(module, true)
                toolWindow.contentManager.setSelectedContent(tabs.modules)
            }

            override fun onNewsOpened() {
                toolWindow.contentManager.setSelectedContent(tabs.overview)
                // The overview may be opening for the first time, with no layout to scroll yet
                application.invokeLater { overviewView.showNews() }
            }
        })
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
        modulesView.viewModelChanged(CourseManager.course(project))
        return modulesView
    }

    private fun createExercisesView(project: Project): ExercisesView {
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

    private fun closeAiAssistantAd(project: Project) {
        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.allEditors.forEach { editor ->
            val file = editor.file ?: return@forEach
            if (file.name == AI_ASSISTANT_AD) {
                CoursesLogger.info("Closing AI Assistant ad")
                fileEditorManager.closeFile(file)
            }
        }
    }
}

@NonNls
private const val AI_ASSISTANT_AD = "AI Assistant"

/** Selection changes report arrival and departure. Only departure marks news read. */
internal fun leavesOverview(event: ContentManagerEvent, overviewTab: Content): Boolean =
    event.operation == ContentManagerEvent.ContentOperation.remove && event.content === overviewTab
