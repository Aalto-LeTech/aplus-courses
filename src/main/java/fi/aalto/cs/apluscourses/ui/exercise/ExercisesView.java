package fi.aalto.cs.apluscourses.ui.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.TreeSpeedSearch;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.OpenItemAction;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import java.awt.CardLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesView {
  private TreeView exerciseGroupsTree;
  private JLabel emptyText;
  private JPanel basePanel;
  @GuiObject
  private JScrollPane pane;
  @GuiObject
  public JPanel toolbarContainer;
  private JPanel cardPanel;
  private CardLayout cl;

  /**
   * Creates an ExerciseView that uses mainViewModel to dynamically adjust its UI components.
   */
  public ExercisesView() {
    basePanel.putClientProperty(ExercisesView.class.getName(), this);
    cl = (CardLayout) cardPanel.getLayout();
    exerciseGroupsTree.getEmptyText().setText("");
    exerciseGroupsTree.setOpaque(true);
    emptyText.setText(getText("ui.exercise.ExercisesView.loading"));
    emptyText.setHorizontalAlignment(SwingConstants.CENTER);
    emptyText.setVerticalAlignment(SwingConstants.CENTER);
    pane.getVerticalScrollBar().setUnitIncrement(exerciseGroupsTree.getRowHeight());
  }

  @NotNull
  public JPanel getBasePanel() {
    return basePanel;
  }

  /**
   * Sets the view model of this view, or does nothing if the given view model is null.
   */
  public void viewModelChanged(@Nullable ExercisesTreeViewModel viewModel) {
    ApplicationManager.getApplication().invokeLater(() -> {
      exerciseGroupsTree.setViewModel(viewModel);
      cl.show(cardPanel, viewModel == null || viewModel.isEmptyTextVisible() ? "LabelCard" :
              "TreeCard");
      if (viewModel == null) {
        return;
      }

      if (viewModel.isProjectReady()) {
        emptyText.setText(getText("ui.module.ModuleListView.turnIntoAPlusProject"));
        if (viewModel.isAuthenticated()) {
          exerciseGroupsTree.getEmptyText().setText(
                  getText("ui.exercise.ExercisesView.allAssignmentsFiltered"));
        } else {
          exerciseGroupsTree.getEmptyText().setText(
                  getText("ui.exercise.ExercisesView.setToken"));
          exerciseGroupsTree.getEmptyText().appendLine(
                  getText("ui.exercise.ExercisesView.setTokenDirections"));
        }
      }

    }, ModalityState.any()
    );
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    exerciseGroupsTree = new TreeView();
    exerciseGroupsTree.setCellRenderer(new ExercisesTreeRenderer());
    exerciseGroupsTree.addNodeAppliedListener(
        ActionUtil.createOnEventLauncher(OpenItemAction.ACTION_ID, exerciseGroupsTree));

    new TreeSpeedSearch(exerciseGroupsTree, treePath -> {
      Searchable treeObject = (Searchable) TreeView.getViewModel(treePath.getLastPathComponent());
      return treeObject.getSearchableString();
    }, true);
  }

  public TreeView getExerciseGroupsTree() {
    return exerciseGroupsTree;
  }

  public JLabel getEmptyTextLabel() {
    return emptyText;
  }

}
