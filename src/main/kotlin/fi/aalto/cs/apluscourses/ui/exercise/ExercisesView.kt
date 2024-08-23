package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.*
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.TextComponentEmptyText
import com.intellij.ui.hover.TreeHoverListener
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.model.exercise.ExerciseGroup
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult
import fi.aalto.cs.apluscourses.services.Opener
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.services.exercise.*
import fi.aalto.cs.apluscourses.ui.Utils.loadingPanel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ScrollPaneConstants
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel


class ExercisesView(project: Project) : SimpleToolWindowPanel(true, true) {

    val exerciseGroupsFilteringTree: ExercisesTreeView = ExercisesTreeView(project).apply {
        installSearchField()
    }

    val searchTextField: SearchTextField
    val scrollPane: JBScrollPane
    val panel = JBUI.Panels.simplePanel()

    init {
        exerciseGroupsFilteringTree.tree.selectionModel.selectionMode =
            TreeSelectionModel.SINGLE_TREE_SELECTION
        TreeUtil.selectFirstNode(exerciseGroupsFilteringTree.tree)

        val treeComponent = exerciseGroupsFilteringTree.component
        scrollPane = JBScrollPane(treeComponent)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS

        exerciseGroupsFilteringTree.updateTree()
        searchTextField = exerciseGroupsFilteringTree.installSearchField()

        panel.add(scrollPane, BorderLayout.CENTER)

        panel.focusTraversalPolicy = ListFocusTraversalPolicy(
            listOf(searchTextField, scrollPane)
        )
        panel.isFocusTraversalPolicyProvider = true
        panel.isFocusCycleRoot = true
        setContent(loadingPanel())
    }


    fun updateTree() {
        setContent(panel)
        val scroll = scrollPane.verticalScrollBar.value
        val expandedItems = getExpandedItems()
        exerciseGroupsFilteringTree.updateTree()
        expandItems(expandedItems)
        scrollPane.verticalScrollBar.value = scroll
        // Show "All items are filtered" if no items are shown
        exerciseGroupsFilteringTree.tree.emptyText.text = "All items are filtered"
    }

    private fun getExpandedItems(): List<ExercisesTreeItem> {
        val tree = exerciseGroupsFilteringTree.tree
        val root = exerciseGroupsFilteringTree.root
        val expandedItems = mutableListOf<ExercisesTreeItem>()

        fun collectExpandedPaths(node: DefaultMutableTreeNode, path: TreePath) {
            if (tree.isExpanded(path)) {
                val userObject = node.userObject as? ExercisesTreeItem ?: return
                expandedItems.add(userObject)
                node.children().toList().forEach { childNode ->
                    collectExpandedPaths(childNode as DefaultMutableTreeNode, path.pathByAddingChild(childNode))
                }
            }
        }

        collectExpandedPaths(root, TreePath(root))
        return expandedItems
    }

    private fun expandItems(expandedItems: List<ExercisesTreeItem>) {
        val tree = exerciseGroupsFilteringTree.tree
        val root = exerciseGroupsFilteringTree.root

        fun expandPaths(node: DefaultMutableTreeNode, path: TreePath) {
            val userObject = node.userObject as? ExercisesTreeItem ?: return
            if (expandedItems.any { it.isSameAs(userObject) }) {
                tree.expandPath(path)
            }
            node.children().toList().forEach { childNode ->
                expandPaths(childNode as DefaultMutableTreeNode, path.pathByAddingChild(childNode))
            }
        }

        expandPaths(root, TreePath(root))
    }

    fun updateExercise(exercise: Exercise) {
        TreeUtil.findNode(exerciseGroupsFilteringTree.root) { node ->
            when (node.userObject) {
                is ExerciseItem -> (node.userObject as ExerciseItem).exercise.id == exercise.id
                else -> false
            }
        }?.let {
            val path = TreeUtil.getPath(exerciseGroupsFilteringTree.root, it)
            TreeUtil.repaintPath(exerciseGroupsFilteringTree.tree, path)
        }
    }

    fun showExercise(exercise: Exercise) {
        searchTextField.text = ""
        TreeUtil.findNode(exerciseGroupsFilteringTree.root) { node ->
            when (node.userObject) {
                is ExerciseItem -> (node.userObject as ExerciseItem).exercise.id == exercise.id
                else -> false
            }
        }?.let {
            val path = TreeUtil.getPath(exerciseGroupsFilteringTree.root, it)
            TreeUtil.selectPath(exerciseGroupsFilteringTree.tree, path)
        }
    }


    sealed interface ExercisesTreeItem {
        fun displayName(): String

        fun url(): String? = null

        fun children(): List<ExercisesTreeItem>

        fun isSameAs(other: ExercisesTreeItem): Boolean
    }

    data class ExercisesRootItem(val project: Project) : ExercisesTreeItem {
        override fun displayName(): String = ""

        private val hiddenElements
            get() = CourseManager.course(project)?.hiddenElements ?: emptyList()

        override fun children(): List<ExerciseGroupItem> =
            ExercisesUpdater.getInstance(project).state.exerciseGroups.map { group ->
                ExerciseGroupItem(
                    group,
                    group.exercises.map { exercise ->
                        val newSubmission =
                            if (exercise.isSubmittable) listOf(NewSubmissionItem(exercise)) else emptyList()
                        ExerciseItem(
                            exercise,
                            (newSubmission + exercise.submissionResults.mapIndexed { i, submission ->
                                SubmissionResultItem(submission, exercise.submissionResults.size - i, exercise)
                            })
                        )
                    }.filterNot( // Filter exercises
                        project.service<ExercisesTreeFilter>().exercisesFilter()
                    ).filterNot {
                        hiddenElements.contains(it.exercise.id)
                    }
                )
            }.filterNot( // Filter groups
                project.service<ExercisesTreeFilter>().exercisesGroupFilter()
            ).filterNot {
                hiddenElements.contains(it.group.id)
            }.filter { group -> // Filter out empty groups
                group.children().isNotEmpty()
            }

        override fun isSameAs(other: ExercisesTreeItem): Boolean = other is ExercisesRootItem
    }

    data class ExerciseGroupItem(val group: ExerciseGroup, private val children: List<ExerciseItem>) :
        ExercisesTreeItem {
        override fun displayName(): String = group.name
        override fun url(): String = group.htmlUrl
        override fun children(): List<ExerciseItem> = children

        override fun isSameAs(other: ExercisesTreeItem): Boolean =
            other is ExerciseGroupItem && other.group.id == group.id
    }

    data class ExerciseItem(val exercise: Exercise, private val children: List<ExercisesTreeItem>) :
        ExercisesTreeItem {
        override fun toString(): String = exercise.name
        override fun displayName(): String = exercise.name
        override fun url(): String = exercise.htmlUrl
        override fun children(): List<ExercisesTreeItem> = children

        override fun isSameAs(other: ExercisesTreeItem): Boolean =
            other is ExerciseItem && other.exercise.id == exercise.id
    }

    data class SubmissionResultItem(
        val submission: SubmissionResult,
        val index: Int,
        val exercise: Exercise
    ) : ExercisesTreeItem {
        override fun displayName(): String = "Submission $index"
        override fun url(): String = "" // Needs to be fetched on demand
        override fun children(): List<ExercisesTreeItem> = emptyList()

        override fun isSameAs(other: ExercisesTreeItem): Boolean =
            other is SubmissionResultItem && other.submission.id == submission.id && other.exercise.id == exercise.id
    }

    data class NewSubmissionItem(val exercise: Exercise) : ExercisesTreeItem {
        override fun displayName(): String =
            if (missingModule) "Show Missing Module: ${exercise.module?.name}"
            else "New submission"

        val missingModule: Boolean
            get() {
                val module = exercise.module ?: return false
                module.updateStatus()
                return exercise.module.status != Component.Status.LOADED
            }

        override fun children(): List<ExercisesTreeItem> = emptyList()

        override fun isSameAs(other: ExercisesTreeItem): Boolean =
            other is NewSubmissionItem && other.exercise.id == exercise.id
    }

    class SimpleExercisesTree : SimpleTree() {
        override fun isFileColorsEnabled(): Boolean = true
        override fun getFileColorForRow(row: Int): Color? =
            if (getPathForRow(row) == null) null
            else if (row % 2 == 0) UIUtil.getTableBackground()
            else if (JBColor.isBright()) UIUtil.getTableBackground().brighter()
            else ColorUtil.darker(UIUtil.getTableBackground(), 1)
    }

    class ExercisesTreeView(private val project: Project) :
        FilteringTree<DefaultMutableTreeNode, ExercisesTreeItem>(
            SimpleExercisesTree(),
            DefaultMutableTreeNode(ExercisesRootItem(project))
        ) {
        init {
            tree.addTreeSelectionListener {
                if (tree.lastSelectedPathComponent == null) {
                    project.service<SelectedExercise>().selectedExercise = null
                    project.service<SelectedExercise>().selectedExerciseTreeItem = null
                    return@addTreeSelectionListener
                }
                val selectedNode = tree.lastSelectedPathComponent as DefaultMutableTreeNode
                val selected = selectedNode.userObject as? ExercisesTreeItem ?: return@addTreeSelectionListener

                val selectedExercise = when (selected) {
                    is ExerciseItem -> selected.exercise
                    is SubmissionResultItem -> selected.exercise
                    is NewSubmissionItem -> selected.exercise
                    else -> null
                }

                project.service<SelectedExercise>().selectedExercise = selectedExercise
                project.service<SelectedExercise>().selectedExerciseTreeItem = selected
            }

            tree.addMouseListener(
                object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        if (e?.clickCount != 2) return
                        activateItem()
                    }
                }
            )

            tree.addKeyListener(
                object : KeyStrokeAdapter() {
                    override fun keyPressed(event: KeyEvent?) {
                        super.keyPressed(event)
                        if (event?.keyCode == VK_ENTER) {
                            activateItem()
                        }
                    }
                }
            )

            tree.isRootVisible = false
            tree.cellRenderer = ExercisesTreeRenderer()
            tree.toggleClickCount = 1
            tree.putClientProperty(AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true) // Enable loading icon animations
            TreeHoverListener.DEFAULT.addTo(tree) // Enable hover color
        }

        private fun activateItem() {
            if (tree.lastSelectedPathComponent == null) return
            val selectedNode = tree.lastSelectedPathComponent as DefaultMutableTreeNode
            val selected = selectedNode.userObject as? ExercisesTreeItem ?: return
            if (selected is NewSubmissionItem) {
                if (selected.missingModule) {
                    project.service<Opener>().showModule(selected.exercise.module!!)
                } else {
                    project.service<SubmitExercise>().submit(selected.exercise)
                }
            } else if (selected is SubmissionResultItem) {
                project.service<ShowFeedback>().showFeedback(selected.submission, selected.exercise)
            }
        }

        fun updateTree() {
            searchModel.updateStructure()
            searchModel.refilter()
            tree.revalidate()
            tree.repaint()
        }

        override fun useIdentityHashing(): Boolean {
            return false
        }

        override fun getNodeClass(): Class<DefaultMutableTreeNode> = DefaultMutableTreeNode::class.java

        override fun getChildren(item: ExercisesTreeItem): List<ExercisesTreeItem> {
            return item.children()
        }

        override fun getText(item: ExercisesTreeItem?): String = when (item) {
            is ExerciseItem -> item.exercise.name
            is SubmissionResultItem -> item.exercise.name
            is NewSubmissionItem -> item.exercise.name
            else -> ""
        }

        override fun expandTreeOnSearchUpdateComplete(pattern: String?) {
            if (pattern?.isNotEmpty() == true) {
                TreeUtil.expand(this.tree, 2) // Don't expand exercises
            }
        }

        override fun onSpeedSearchUpdateComplete(pattern: String?) {
            if (pattern?.isEmpty() == true) {
                TreeUtil.collapseAll(tree, 1) // Collapse everything when search is cleared
            }
        }

        override fun createNode(item: ExercisesTreeItem): DefaultMutableTreeNode =
            DefaultMutableTreeNode(item)

        override fun installSearchField(): SearchTextField {
            return super.installSearchField().apply {
                textEditor.apply {
                    emptyText.text = "Search Assignments..."
                    accessibleContext.accessibleName = "Search Assignments"
                    TextComponentEmptyText.setupPlaceholderVisibility(this)
                }
            }
        }
    }
}
