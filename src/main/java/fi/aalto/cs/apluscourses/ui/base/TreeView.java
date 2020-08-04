package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.ui.treeStructure.Tree;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreeView extends Tree {
  private final transient Set<ActionListener> nodeAppliedListeners = ConcurrentHashMap.newKeySet();

  @Nullable
  protected transient volatile SelectableNodeViewModel<?> selectedItem = null; //NOSONAR

  /**
   * Construct an empty tree with no nodes and the root set to invisible.
   */
  public TreeView() {
    setRootVisible(false);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    addTreeSelectionListener(new SelectionListener());
    addMouseListener(new TreeMouseListener());
  }

  /**
   * Set the model of the this tree to the given view model, or do nothing if the given view model
   * is {@code null}.
   */
  public void setViewModel(@Nullable TreeViewModel viewModel) {
    if (viewModel != null) {
      setModel(new DefaultTreeModel(createNode(viewModel)));
    }
    selectedItem = null;
  }

  @NotNull
  private static DefaultMutableTreeNode createNode(@NotNull TreeViewModel tree) {
    List<? extends TreeViewModel> subtrees = tree.getSubtrees();
    boolean allowsChildren = subtrees != null;
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(tree, allowsChildren);
    if (allowsChildren) {
      for (TreeViewModel subtree : subtrees) {
        node.add(createNode(subtree));
      }
    }
    return node;
  }

  public void addNodeAppliedListener(ActionListener listener) {
    nodeAppliedListeners.add(listener);
  }

  public void removeNodeAppliedListener(ActionListener listener) {
    nodeAppliedListeners.remove(listener);
  }

  protected class SelectionListener implements TreeSelectionListener {

    @Override
    public void valueChanged(@NotNull TreeSelectionEvent e) {
      SelectableNodeViewModel<?> oldViewModel = getViewModelFromPath(e.getOldLeadSelectionPath());
      if (oldViewModel != null) {
        oldViewModel.setSelected(false);
      }
      SelectableNodeViewModel<?> newViewModel = getViewModelFromPath(e.getNewLeadSelectionPath());
      if (newViewModel != null) {
        newViewModel.setSelected(true);
      }
      selectedItem = newViewModel;
    }

    @Nullable
    private SelectableNodeViewModel<?> getViewModelFromPath(@Nullable TreePath treePath) {
      if (treePath == null || treePath.getPathCount() <= 1) {
        return null; // Null for the root node, since it is hidden.
      }
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
      return (SelectableNodeViewModel<?>) node.getUserObject();
    }
  }

  protected class TreeMouseListener extends MouseAdapter {

    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
      if (e.getClickCount() == 2 && selectedItem != null) {
        ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
        nodeAppliedListeners.forEach(listener -> listener.actionPerformed(actionEvent));
      }
    }
  }
}
