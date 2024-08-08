package fi.aalto.cs.apluscourses.ui.exercise

import com.intellij.icons.AllIcons
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.packageDependencies.ui.DependenciesPanel.DependencyPanelSettings
import com.intellij.packageDependencies.ui.FileNode
import com.intellij.packageDependencies.ui.FileTreeModelBuilder
import com.intellij.packageDependencies.ui.Marker
import com.intellij.psi.PsiManager
import com.intellij.ui.JBColor
import com.intellij.ui.RoundedLineBorder
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.treeStructure.Tree
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.model.exercise.Exercise
import fi.aalto.cs.apluscourses.utils.temp.FileDateFormatter
import icons.PluginIcons
import java.nio.file.Path
import javax.swing.BorderFactory
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath
import kotlin.io.path.name

class SubmitExerciseDialog(val project: Project, val exercise: Exercise, val files: List<Path>) :
    DialogWrapper(project) {
    init {
        setOKButtonText("Submit")
        title = "Submit ${exercise.name}"
        setSize(0, 0)
        init()
    }

    // TODO check if can submit with group (if has submitted with group before)

    override fun createCenterPanel(): DialogPanel = panel {
//        row {
//            label(exercise.name).bold()
//        }
        val submissionNumber = exercise.submissionResults.size + 1

        row {
            label("Files to submit:")
        }
        row {
            cell(FileTree()).applyToComponent {
                cellRenderer = FileRenderer(files.associate { it.name to it })
                isEnabled = false
            }
        }
        row("Group:") {
            comboBox(listOf("Submit alone", "TODO", "Group 1", "Group 2"))
            button(
                "Set as default"
            ) {}
        }
        row {
            text("You are about to make submission ${submissionNumber} out of ${exercise.maxSubmissions}.")
        }
        row {
            if (exercise.maxSubmissions <= (submissionNumber)) {
                text(
                    if (submissionNumber == exercise.maxSubmissions) {
                        MyBundle.message("presentation.submissionViewModel.warning.lastSubmission")
                    } else {
                        MyBundle.message("presentation.submissionViewModel.warning.submissionsExceeded")
                    }
                ).applyToComponent {
                    foreground = JBColor.namedColor("Notification.ToolWindow.errorForeground")
                    background = JBColor.namedColor("Notification.ToolWindow.errorBackground")
                    isOpaque = true
                    val errorBorderColor = JBColor.namedColor("Notification.ToolWindow.errorBorderColor")

                    border = BorderFactory.createCompoundBorder(
                        RoundedLineBorder(errorBorderColor, 10, 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    )
                }
            }
        }
    }

//    data class FileInfo(val name: String, val path: String)

    inner class FileTree : Tree() {
        init {
            val files = files.mapNotNull { file ->
                val vFile = VirtualFileManager.getInstance().findFileByNioPath(file) ?: return@mapNotNull null
                val psiFile = PsiManager.getInstance(project).findFile(vFile) ?: return@mapNotNull null
                psiFile
            }.toSet()
            val settings = DependencyPanelSettings()
            settings.UI_SHOW_MODULES = false
            val model = FileTreeModelBuilder.createTreeModel(
                project, false, files,
                object : Marker {
                    override fun isMarked(file: VirtualFile): Boolean = false
                }, settings
            )

            this.model = model
            DefaultTreeExpander(this).expandAll()
            isRootVisible = false
            this.rootPane
            setUI(object : BasicTreeUI() {
                override fun shouldPaintExpandControl(
                    path: TreePath?,
                    row: Int,
                    isExpanded: Boolean,
                    hasBeenExpanded: Boolean,
                    isLeaf: Boolean
                ): Boolean = false
            })
        }
    }

    inner class FileRenderer(private val files: Map<String, Path>) : NodeRenderer() {
        override fun customizeCellRenderer(
            tree: JTree,
            value: Any,
            isSelected: Boolean,
            isExpanded: Boolean,
            isLeaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ) {
            val node = value as DefaultMutableTreeNode
            val text = node.userObject as String
            if (node is FileNode) {
                icon = node.psiElement?.getIcon(0)
                append(text)
                append(
                    " last modified ${FileDateFormatter.getFileModificationTime(files[text]!!)}", //TODO
                    SimpleTextAttributes.GRAYED_ATTRIBUTES
                )
            } else {
                icon = if (row == 0) PluginIcons.A_PLUS_MODULE else AllIcons.Nodes.Package
                append(text.replace("/", "."))
            }

        }
    }

}