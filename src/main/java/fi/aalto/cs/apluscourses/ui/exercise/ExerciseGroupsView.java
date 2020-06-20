package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.ui.treeStructure.Tree;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import javax.swing.JPanel;
import javax.swing.JTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExerciseGroupsView {
  private JTree exerciseGroupsTree;
  private JPanel basePanel;

  public ExerciseGroupsView() {
    // See ModulesView.java
    basePanel.putClientProperty(ExerciseGroupsView.class.getName(), this);
  }

  @NotNull
  public JPanel getBasePanel() {
    return basePanel;
  }

  /**
   * Sets the view model of this view, or does nothing if the given view model is null.
   */
  public void viewModelChanged(@Nullable ExercisesTreeViewModel viewModel) {
    if (viewModel == null) {
      return;
    }
    exerciseGroupsTree.setModel(viewModel.toTreeModel());
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    exerciseGroupsTree = new Tree();
    exerciseGroupsTree.setCellRenderer(new ExercisesTreeRenderer());
  }
}
