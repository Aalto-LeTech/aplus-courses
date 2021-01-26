package fi.aalto.cs.apluscourses.ui.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.OpenSubmissionAction;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
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
  private JScrollPane pane;
  @GuiObject
  public JPanel toolbarContainer;
  private JPanel cardPanel;
  private CardLayout cl;
  private MainViewModel mainViewModel;
  private boolean hasCourse = false;

  /**
   * Creates an ExerciseView that uses mainViewModel to dynamically adjust its UI components.
   */
  public ExercisesView(MainViewModel mainViewModel) {
    this.mainViewModel = mainViewModel;
    mainViewModel.exercisesViewModel
            .addValueObserver(this, ExercisesView::viewModelChanged);
    basePanel.putClientProperty(ExercisesView.class.getName(), this);
    cl = (CardLayout) cardPanel.getLayout();
    exerciseGroupsTree.getEmptyText().appendLine(getText("ui.exercise.ExercisesView.setToken"));
    exerciseGroupsTree.getEmptyText().appendLine(
            getText("ui.exercise.ExercisesView.setTokenDirections"));
    updateComponents();
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
      hasCourse = mainViewModel.getHasCourse();
      updateComponents();
    },
        ModalityState.any()
    );
  }

  private void updateComponents() {
    emptyText.setText(getText("ui.module.ModuleListView.turnIntoAPlusProject"));
    emptyText.setHorizontalAlignment(SwingConstants.CENTER);
    emptyText.setVerticalAlignment(SwingConstants.CENTER);
    if (hasCourse) {
      cl.show(cardPanel,"TreeCard");
    } else {
      cl.show(cardPanel, "LabelCard");
    }
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
