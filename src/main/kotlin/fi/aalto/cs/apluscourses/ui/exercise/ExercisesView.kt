package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.*
import com.intellij.ui.components.TextComponentEmptyText
import com.intellij.ui.hover.TreeHoverListener
import com.intellij.ui.scale.JBUIScale.scale
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.services.exercise.ExercisesTreeFilterService
import fi.aalto.cs.apluscourses.services.exercise.ExercisesUpdaterService
import fi.aalto.cs.apluscourses.services.exercise.SelectedExerciseService
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.ScrollPaneConstants
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel


class ExercisesView(project: Project) :
    SimpleToolWindowPanel(true, true) {

    val exerciseGroupsFilteringTree: ExercisesTreeView = ExercisesTreeView(project).apply {
        installSearchField()
    }

    val searchTextField: SearchTextField

    /**
     * Creates an ExerciseView that uses mainViewModel to dynamically adjust its UI components.
     */
    init {
        exerciseGroupsFilteringTree.tree.selectionModel.selectionMode =
            TreeSelectionModel.SINGLE_TREE_SELECTION
        TreeUtil.selectFirstNode(exerciseGroupsFilteringTree.tree)

        val treeComponent = exerciseGroupsFilteringTree.component
        val scrollPane = ScrollPaneFactory.createScrollPane(treeComponent, true)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS

        exerciseGroupsFilteringTree.updateTree()
        searchTextField = exerciseGroupsFilteringTree.installSearchField()

        val panel = JBUI.Panels.simplePanel()
//        panel.add(searchTextField, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        panel.focusTraversalPolicy = ListFocusTraversalPolicy(
            listOf(searchTextField, scrollPane)
        )
        panel.isFocusTraversalPolicyProvider = true
        panel.isFocusCycleRoot = true
        setContent(panel)
    }


    fun updateTree() {
//        val scroll = scrollPane.verticalScrollBar.value
//        val treeState = TreeState.createOn(exerciseGroupsFilteringTree.tree, exerciseGroupsFilteringTree.root)
//        invokeLater {
        exerciseGroupsFilteringTree.updateTree()
//            panel.remove(searchTextField)
//            searchTextField.removeNotify()
//            searchTextField = exerciseGroupsFilteringTree.installSearchField()
//            panel.add(searchTextField, BorderLayout.NORTH)
//            panel.revalidate()
//            panel.repaint()
//        }

//        treeState.applyTo(exerciseGroupsFilteringTree.tree)
//        scrollPane.verticalScrollBar.value = scroll
    }

    fun updateExercise(exercise: Exercise) {
//        return
        TreeUtil.findNode(exerciseGroupsFilteringTree.root) { node ->
            when (node.userObject) {
                is ExerciseItem -> (node.userObject as ExerciseItem).exercise.id == exercise.id
                else -> false
            }
        }?.let {
            val path = TreeUtil.getPath(exerciseGroupsFilteringTree.root, it)
            TreeUtil.repaintPath(exerciseGroupsFilteringTree.tree, path)
//            exerciseGroupsFilteringTree.tree.repaint()
        }
    }


    sealed interface ExercisesTreeItem {
        fun displayName(): String

        fun children(): List<ExercisesTreeItem>
    }

    data class ExercisesRootItem(val project: Project) : ExercisesTreeItem {
        override fun displayName(): String = ""

        override fun children(): List<ExerciseGroupItem> = //listOf()
            project.service<ExercisesUpdaterService>().state.exerciseGroups.map { group ->
                ExerciseGroupItem(
                    group,
                    group.exercises.map { exercise ->
                        val newSubmission =
                            if (exercise.isSubmittable) listOf(NewSubmissionItem(exercise)) else emptyList()
                        ExerciseItem(
                            exercise,
                            (exercise.submissionResults.mapIndexed { i, submission ->
                                SubmissionResultItem(submission, i, exercise)
                            } + newSubmission
                                    ).reversed() // Reverse order of submissions to show the latest first
                        )
                    })
            }.filterNot( // Filter groups
                ApplicationManager.getApplication()
                    .service<ExercisesTreeFilterService>().state.exercisesGroupFilter()
            ).filter { group -> // Filter out empty groups
                group.children().isNotEmpty()
            }
    }

    data class ExerciseGroupItem(val group: ExerciseGroup, private val children: List<ExerciseItem>) :
        ExercisesTreeItem {
        override fun displayName(): String = group.name

        override fun children(): List<ExerciseItem> = children.filterNot( // Filter exercises
            ApplicationManager.getApplication()
                .service<ExercisesTreeFilterService>().state.exercisesFilter()
        )
    }

    data class ExerciseItem(val exercise: Exercise, private val children: List<ExercisesTreeItem>) :
        ExercisesTreeItem {
        override fun displayName(): String = exercise.name

        override fun children(): List<ExercisesTreeItem> = children
    }

    data class SubmissionResultItem(
        val submission: SubmissionResult,
        val index: Int,
        val exercise: Exercise
    ) :
        ExercisesTreeItem {
        override fun displayName(): String = "Submission ${index + 1}"

        override fun children(): List<ExercisesTreeItem> = emptyList()
    }

    data class NewSubmissionItem(val exercise: Exercise) : ExercisesTreeItem {
        override fun displayName(): String = "New submission"

        override fun children(): List<ExercisesTreeItem> = emptyList()
    }

    class SimpleExercisesTree : SimpleTree() {
        override fun isFileColorsEnabled(): Boolean = true
        override fun getFileColorForRow(row: Int): Color? =
            if (getPathForRow(row) == null) null
            else if (row % 2 == 0) UIUtil.getTableBackground()
            else if (JBColor.isBright()) UIUtil.getTableBackground().brighter()
            else ColorUtil.darker(UIUtil.getTableBackground(), 1)
    }

    class ExercisesTreeView(project: Project) :
        FilteringTree<DefaultMutableTreeNode, ExercisesTreeItem>(
            SimpleExercisesTree(),
            DefaultMutableTreeNode(ExercisesRootItem(project))
        ) {
        init {
            tree.addTreeSelectionListener {
                if (tree.lastSelectedPathComponent == null) return@addTreeSelectionListener
                val selectedNode = tree.lastSelectedPathComponent as DefaultMutableTreeNode
                val selected = selectedNode.userObject as? ExercisesTreeItem ?: return@addTreeSelectionListener

                val selectedExercise = when (selected) {
                    is ExerciseItem -> selected.exercise
                    is SubmissionResultItem -> selected.exercise
                    is NewSubmissionItem -> selected.exercise
                    else -> null
                }

                project.service<SelectedExerciseService>().selectedExercise = selectedExercise
            }
            tree.isRootVisible = false
            tree.cellRenderer = ExercisesTreeRenderer()
            tree.rowHeight = scale(24)
            tree.toggleClickCount = 1
            tree.putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
            SmartExpander.installOn(tree)
            TreeHoverListener.DEFAULT.addTo(tree)
        }

        fun updateTree() {
            searchModel.updateStructure()
            tree.revalidate()
            tree.repaint()
        }

        override fun useIdentityHashing(): Boolean {
            return false
        }

        override fun getNodeClass(): Class<DefaultMutableTreeNode> = DefaultMutableTreeNode::class.java

        override fun getChildren(item: ExercisesTreeItem): List<ExercisesTreeItem> =
            item.children()

        override fun getText(item: ExercisesTreeItem?): String = when (item) {
            is ExerciseItem -> item.exercise.name
            is SubmissionResultItem -> item.exercise.name
            is NewSubmissionItem -> item.exercise.name
            else -> ""
        }

        override fun createNode(item: ExercisesTreeItem): DefaultMutableTreeNode =
            DefaultMutableTreeNode(item)

        override fun installSearchField(): SearchTextField {
            return super.installSearchField().apply {
                textEditor.apply {
                    emptyText.text = "Search"
                    accessibleContext.accessibleName = "Search"
                    TextComponentEmptyText.setupPlaceholderVisibility(this)

//                    addKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)) { activateItems(tree) }
                }
            }
        }

    }


}


/**
 * Sets the nodeAppliedListener as OpenExerciseItemAction if the course isn't supported in ShowFeedbackAction,
 * else ShowFeedbackAction.
 */
//    fun setSubmissionAction(feedbackEnabled: Boolean) {
//        if (!feedbackEnabled) {
//            exerciseGroupsTree.addNodeAppliedListener(
//                SubmissionResultViewModel::class.java,
//                ActionUtil.createOnEventLauncher(OpenExerciseItemAction.ACTION_ID, exerciseGroupsTree)
//            )
//        } else {
//            exerciseGroupsTree.addNodeAppliedListener(
//                SubmissionResultViewModel::class.java,
//                ActionUtil.createOnEventLauncher(ShowFeedbackAction.ACTION_ID, exerciseGroupsTree)
//            )
//        }
//    }