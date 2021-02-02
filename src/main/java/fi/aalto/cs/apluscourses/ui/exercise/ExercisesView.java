package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.TreeSpeedSearch;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.OpenItemAction;
import fi.aalto.cs.apluscourses.presentation.base.SearchableNode;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
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
    ApplicationManager.getApplication().invokeLater(
        () -> exerciseGroupsTree.setViewModel(viewModel),
        ModalityState.any()
    );
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    exerciseGroupsTree = new TreeView();
    exerciseGroupsTree.setCellRenderer(new ExercisesTreeRenderer());
    exerciseGroupsTree.addNodeAppliedListener(
        ActionUtil.createOnEventLauncher(OpenItemAction.ACTION_ID, exerciseGroupsTree));

    new TreeSpeedSearch(exerciseGroupsTree, x -> {
      SearchableNode treeObject = (SearchableNode) TreeView.getViewModel(x.getLastPathComponent());
      return treeObject.getSearchableString();
    });
  }

  public TreeView getExerciseGroupsTree() {
    return exerciseGroupsTree;
  }
}
