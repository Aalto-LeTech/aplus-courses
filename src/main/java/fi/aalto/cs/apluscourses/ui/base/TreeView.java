package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.ui.treeStructure.Tree;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that extends {@link Tree} and works with {@link SelectableNodeViewModel}. The root of the
 * tree is hidden. Only one node of the tree may be selected at once. The nodes in the tree model
 * must be instances of {@link DefaultMutableTreeNode}.
 */
public class TreeView extends Tree {

  /**
   * Construct an empty tree.
   */
  public TreeView() {
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    addTreeSelectionListener(new SelectionListener());
    addMouseListener(new TreeMouseListener());
  }

  /**
   * Set the model of this tree to the given model, or do nothing if the given model is {@code
   * null}. The nodes of the model must be instances of {@link DefaultMutableTreeNode}.
   */
  public void setViewModel(@Nullable TreeViewModel viewModel) {
    if (viewModel != null) {
      setModel(viewModel.toTreeModel());
    }
  }

  public void onTreeActionPerformed() {
    throw new UnsupportedOperationException();
  }

  private class SelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(@NotNull TreeSelectionEvent e) {
      TreePath oldSelection = e.getOldLeadSelectionPath();
      // Don't do anything if this is the root node, since it is hidden.
      if (oldSelection != null && oldSelection.getPathCount() > 1) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) oldSelection.getLastPathComponent();
        SelectableNodeViewModel<?> viewModel = (SelectableNodeViewModel) node.getUserObject();
        viewModel.setSelected(false);
      }

      TreePath newSelection = e.getNewLeadSelectionPath();
      // Don't do anything if this is the root node, since it is hidden.
      if (newSelection != null && newSelection.getPathCount() > 1) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) newSelection.getLastPathComponent();
        SelectableNodeViewModel<?> viewModel = (SelectableNodeViewModel) node.getUserObject();
        viewModel.setSelected(true);
      }
    }
  }

  private class TreeMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
      if (e.getClickCount() == 2) {
        onTreeActionPerformed();
      }
    }
  }
}
