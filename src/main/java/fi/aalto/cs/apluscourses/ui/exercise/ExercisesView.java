package fi.aalto.cs.apluscourses.ui.exercise;

import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.SubmitExerciseAction;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesView {
  private TreeView exerciseGroupsTree;

  private JPanel basePanel;

  @GuiObject
  public JPanel toolbarContainer;

  private JLabel tokenReminder;

  private JScrollPane treeHolder;


  public ExercisesView() {
    // See ModulesView.java
    basePanel.putClientProperty(ExercisesView.class.getName(), this);
  }

  @NotNull
  public JPanel getBasePanel() {
    return basePanel;
  }

  /**
   * Sets the view model of this view.
   */
  public void viewModelChanged(@Nullable ExercisesTreeViewModel viewModel) {
    boolean noView = viewModel == null;
    tokenReminder.setVisible(noView);
    treeHolder.setVisible(!noView);
    exerciseGroupsTree.setViewModel(viewModel);
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    exerciseGroupsTree = new TreeView();
    exerciseGroupsTree.setCellRenderer(new ExercisesTreeRenderer());
    exerciseGroupsTree.addNodeAppliedListener(ActionUtil.createOnEventLauncher(
        SubmitExerciseAction.ACTION_ID, exerciseGroupsTree));
  }

  public TreeView getExerciseGroupsTree() {
    return exerciseGroupsTree;
  }
}
