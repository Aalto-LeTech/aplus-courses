package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.ui.treeStructure.Tree;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.SubmitExerciseAction;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.TreeViewModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesTreeView extends Tree {

  private int selectedNodePathCount;
  private ActionListener onNodeApplied;

  /**
   * Construct an empty tree with no nodes and the root set to invisible.
   */
  public ExercisesTreeView() {
    selectedNodePathCount = 0;
    onNodeApplied = ActionUtil.createOnEventLauncher(SubmitExerciseAction.ACTION_ID, this);
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
      setModel(viewModel.toTreeModel());
    }
  }

  private class SelectionListener implements TreeSelectionListener {

    @Override
    public void valueChanged(@NotNull TreeSelectionEvent e) {
      selectedNodePathCount = 0;

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
        selectedNodePathCount = newSelection.getPathCount();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) newSelection.getLastPathComponent();
        SelectableNodeViewModel<?> viewModel = (SelectableNodeViewModel) node.getUserObject();
        viewModel.setSelected(true);
      }
    }

  }

  private class TreeMouseListener extends MouseAdapter {

    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
      if (e.getClickCount() == 2 && selectedNodePathCount > 2) {
        ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
        onNodeApplied.actionPerformed(actionEvent);
      }
    }

  }
}
