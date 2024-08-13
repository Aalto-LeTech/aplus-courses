package fi.aalto.cs.apluscourses.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.packageDependencies.ui.DependenciesPanel.DependencyPanelSettings
import com.intellij.packageDependencies.ui.FileNode
import com.intellij.packageDependencies.ui.FileTreeModelBuilder
import com.intellij.packageDependencies.ui.Marker
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.PsiIconUtil
import fi.aalto.cs.apluscourses.services.Background
import fi.aalto.cs.apluscourses.utils.temp.FileDateFormatter
import icons.PluginIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

class FileTree(files: List<Path>, project: Project) : Tree() {
    var icons: Map<PsiFile, Icon> = emptyMap()

    // The tree expand controls are hidden, so the position of the tree is adjusted
    override fun getY(): Int = super.y - 5
    override fun getX(): Int = super.x - 16

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
                    object : Marker {
                        override fun isMarked(file: VirtualFile): Boolean = false
                    }, settings
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
            icon = tree.icons[node.psiElement]
            append(text)
            val file = files[text]
            if (file != null) {
                append(
                    " last modified ${FileDateFormatter.getFileModificationTime(file)}",
                    SimpleTextAttributes.GRAYED_ATTRIBUTES
                )
            }
        } else {
            icon = if (row == 0) PluginIcons.A_PLUS_MODULE else AllIcons.Nodes.Package
            append(text.replace("/", ".")) // Use periods for package separators
        }

    }
}