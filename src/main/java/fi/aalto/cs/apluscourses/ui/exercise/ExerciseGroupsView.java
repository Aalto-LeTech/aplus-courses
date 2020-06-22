package fi.aalto.cs.apluscourses.ui.exercise;

import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExerciseGroupsView {
  private TreeView exerciseGroupsTree;
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
    exerciseGroupsTree.setViewModel(viewModel);
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    exerciseGroupsTree = new TreeView();
    exerciseGroupsTree.setCellRenderer(new ExercisesTreeRenderer());
  }


}
