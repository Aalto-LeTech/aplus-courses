package fi.aalto.cs.apluscourses.ui.exercise;

import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.OpenSubmissionAction;
import fi.aalto.cs.apluscourses.intellij.actions.SubmitExerciseAction;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesView {
  private TreeView exerciseGroupsTree;
  private JPanel basePanel;

  @GuiObject
  public JPanel toolbarContainer;

  public ExercisesView() {
    // See ModulesView.java
    basePanel.putClientProperty(ExercisesView.class.getName(), this);
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
    exerciseGroupsTree.addNodeAppliedListener(
        ActionUtil.createOnEventLauncher(OpenSubmissionAction.ACTION_ID, exerciseGroupsTree));
  }

  public TreeView getExerciseGroupsTree() {
    return exerciseGroupsTree;
  }
}
