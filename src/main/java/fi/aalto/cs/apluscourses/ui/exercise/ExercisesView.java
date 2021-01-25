package fi.aalto.cs.apluscourses.ui.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.OpenSubmissionAction;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.base.TreeView;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;

public class ExercisesView {
  private TreeView exerciseGroupsTree;
  private JLabel emptyText;
  private JPanel basePanel;
  private JScrollPane pane;
  @GuiObject
  public JPanel toolbarContainer;

  public ExercisesView() {
    basePanel.putClientProperty(ExercisesView.class.getName(), this);
    updateComponents();
    // See ModulesView.java
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
      updateComponents();
    },
        ModalityState.any()
    );
    updateComponents();
  }

  private void updateComponents() {
    emptyText.setText(getText("ui.module.ModuleListView.turnIntoAPlusProject"));
    emptyText.setHorizontalAlignment(SwingConstants.LEFT);
    emptyText.setVerticalAlignment(SwingConstants.TOP);
    if (exerciseGroupsTree.getChildCount(exerciseGroupsTree.getModel()) <= 1) {
      exerciseGroupsTree.setVisible(false);
      pane.setVisible(false);
      emptyText.setVisible(true);
    } else {
      emptyText.setVisible(false);
      exerciseGroupsTree.setVisible(true);
      pane.setVisible(true);
    }
    pane.revalidate();
    basePanel.repaint();
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

  public JLabel getEmptyTextLabel() {
    return emptyText;
  }
}
