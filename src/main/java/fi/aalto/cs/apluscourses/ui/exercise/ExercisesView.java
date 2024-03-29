package fi.aalto.cs.apluscourses.ui.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.util.ui.tree.TreeUtil;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.OpenExerciseItemAction;
import fi.aalto.cs.apluscourses.intellij.actions.ShowFeedbackAction;
import fi.aalto.cs.apluscourses.intellij.actions.SubmitExerciseAction;
import fi.aalto.cs.apluscourses.model.ExercisesTree;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.Searchable;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmitExerciseViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.ToolbarPanel;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExercisesView implements ToolbarPanel {
  private TreeView exerciseGroupsTree;
  private JPanel basePanel;
  @GuiObject
  private JScrollPane pane;
  @GuiObject
  public JPanel toolbarContainer;
  @GuiObject
  private JLabel title;

  /**
   * Creates an ExerciseView that uses mainViewModel to dynamically adjust its UI components.
   */
  public ExercisesView() {
    basePanel.putClientProperty(ExercisesView.class.getName(), this);
    pane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()));
    exerciseGroupsTree.getEmptyText().setText("");
    exerciseGroupsTree.setOpaque(true);
    exerciseGroupsTree.getEmptyText().setText(getText("ui.toolWindow.loading"));
    var rowHeight = exerciseGroupsTree.getRowHeight();
    // Row height returns <= 0 on some platforms, so a default alternative is needed
    pane.getVerticalScrollBar().setUnitIncrement(rowHeight <= 0 ? 20 : rowHeight);
  }

  @Override
  @NotNull
  public JPanel getBasePanel() {
    return basePanel;
  }

  @Override
  @NotNull
  public JPanel getToolbar() {
    return toolbarContainer;
  }

  @NotNull
  @Override
  public String getTitle() {
    return getText("ui.toolWindow.subTab.exercises.name");
  }

  /**
   * Sets the view model of this view, or does nothing if the given view model is null.
   */
  public void viewModelChanged(@Nullable ExercisesTreeViewModel viewModel) {
    ApplicationManager.getApplication().invokeLater(() -> {
          exerciseGroupsTree.setViewModel(viewModel);
          exerciseGroupsTree.getEmptyText().setText(
              getText("ui.toolWindow.loading"));
          if (viewModel == null) {
            return;
          }

          exerciseGroupsTree.getEmptyText().setText(viewModel.getEmptyText());
          title.setText(viewModel.getTitleText());

        }, ModalityState.any()
    );
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    pane = ScrollPaneFactory.createScrollPane(basePanel);
    title = new JLabel();
    exerciseGroupsTree = new ExercisesTreeView();
    exerciseGroupsTree.setCellRenderer(new ExercisesTreeRenderer());
    exerciseGroupsTree.addNodeAppliedListener(SubmitExerciseViewModel.class,
        ActionUtil.createOnEventLauncher(SubmitExerciseAction.ACTION_ID, exerciseGroupsTree));

    new TreeSpeedSearch(exerciseGroupsTree, treePath -> {
      Searchable treeObject = (Searchable) TreeView.getViewModel(treePath.getLastPathComponent());
      return treeObject.getSearchableString();
    }, true);
  }

  public TreeView getExerciseGroupsTree() {
    return exerciseGroupsTree;
  }

  /**
   * Sets the nodeAppliedListener as OpenExerciseItemAction if the course isn't supported in ShowFeedbackAction,
   * else ShowFeedbackAction.
   */
  public void setSubmissionAction(boolean feedbackEnabled) {
    if (!feedbackEnabled) {
      exerciseGroupsTree.addNodeAppliedListener(SubmissionResultViewModel.class,
          ActionUtil.createOnEventLauncher(OpenExerciseItemAction.ACTION_ID, exerciseGroupsTree));
    } else {
      exerciseGroupsTree.addNodeAppliedListener(SubmissionResultViewModel.class,
          ActionUtil.createOnEventLauncher(ShowFeedbackAction.ACTION_ID, exerciseGroupsTree));
    }
  }

  private static class ExercisesTreeView extends TreeView {
    @Override
    public void setViewModel(@Nullable BaseTreeViewModel<?> viewModel) {
      var oldViewModel = getViewModel();
      if (viewModel != null && oldViewModel != null) {
        var oldModel = oldViewModel.getModel();
        var newModel = viewModel.getModel();
        if (oldModel instanceof ExercisesTree
            && newModel instanceof ExercisesTree
            && ((ExercisesTree) oldModel).getSelectedStudent()
            != ((ExercisesTree) newModel).getSelectedStudent()) {
          TreeUtil.collapseAll(this, 1);
        }
      }
      super.setViewModel(viewModel);
    }
  }

}
