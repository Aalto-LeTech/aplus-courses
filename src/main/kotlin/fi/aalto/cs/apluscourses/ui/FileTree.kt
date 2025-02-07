package fi.aalto.cs.apluscourses.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.packageDependencies.ui.DependenciesPanel.DependencyPanelSettings
import com.intellij.packageDependencies.ui.FileNode
import com.intellij.packageDependencies.ui.FileTreeModelBuilder
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.PsiIconUtil
import com.intellij.util.application
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.icons.CoursesIcons
import fi.aalto.cs.apluscourses.services.Background
import fi.aalto.cs.apluscourses.utils.FileDateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class FileTree(files: List<Path>, project: Project) : Tree() {
    var icons: Map<PsiFile, Icon> = emptyMap()

    init {
        val settings = DependencyPanelSettings()
        settings.UI_SHOW_MODULES = false // The module has the same name as the top level package, so it is hidden
        project.service<Background>().runInBackground {
            val files = files.mapNotNull { file ->
                val vFile = VirtualFileManager.getInstance().findFileByNioPath(file) ?: return@mapNotNull null
                val psiFile =
                    readAction {
                        PsiManager.getInstance(project).findFile(vFile)
                    } ?: return@mapNotNull null
                psiFile
            }.toSet()

            val model = withContext(Dispatchers.IO) {
                FileTreeModelBuilder.createTreeModel(
                    project, false, files,
                    { false }, settings
                )
            }

            icons = withContext(Dispatchers.IO) {
                files.mapNotNull {
                    val icon = readAction { PsiIconUtil.getIconFromProviders(it, flags = 0) } ?: return@mapNotNull null
                    it to icon
                }.toMap()
            }

            this.model = model
            DefaultTreeExpander(this).expandAll()
            isRootVisible = false
        }
    }
}

class FileRenderer(private val files: Map<String, Path>) : NodeRenderer() {
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
        val text = "${node.userObject}"
        val tree = tree as FileTree
        if (node is FileNode) {
            application.runReadAction {
                icon = tree.icons[node.psiElement]
            }
            append(text)
            val file = files[text]
            if (file != null) {
                append(
                    " " + message("ui.FileTree.lastModified", FileDateFormatter.getFileModificationTime(file)),
                    SimpleTextAttributes.GRAYED_ATTRIBUTES
                )
            }
        } else {
            icon = if (row == 0) CoursesIcons.Module else AllIcons.Nodes.Package
            append(text.replace("/", ".")) // Use periods for package separators
        }

    }
}