package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import icons.PluginIcons;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jetbrains.annotations.NotNull;

public class ExercisesTreeRenderer extends ColoredTreeCellRenderer {

  @NotNull
  private static Icon statusToIcon(@NotNull ExerciseViewModel.Status exerciseStatus) {
    switch (exerciseStatus) {
      case OPTIONAL_PRACTICE:
        return PluginIcons.A_PLUS_OPTIONAL_PRACTICE;
      case NO_SUBMISSIONS:
        return PluginIcons.A_PLUS_NO_SUBMISSIONS;
      case NO_POINTS:
        return PluginIcons.A_PLUS_NO_POINTS;
      case PARTIAL_POINTS:
        return PluginIcons.A_PLUS_PARTIAL_POINTS;
      case FULL_POINTS:
        return PluginIcons.A_PLUS_FULL_POINTS;
      default:
        throw new IllegalStateException("Invalid exercise view model status");
    }
  }

  private static final SimpleTextAttributes STATUS_TEXT_STYLE = new SimpleTextAttributes(
      SimpleTextAttributes.STYLE_ITALIC | SimpleTextAttributes.STYLE_SMALLER, null);

  @Override
  public void customizeCellRenderer(@NotNull JTree tree,
                                    Object value,
                                    boolean isSelected,
                                    boolean isExpanded,
                                    boolean isLeaf,
                                    int row,
                                    boolean hasFocus) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object userObject = node.getUserObject();
    if (userObject == null || userObject instanceof ExercisesTreeViewModel) {
      // This is the root node, which is hidden anyways.
      return;
    }

    if (userObject instanceof ExerciseViewModel) {
      ExerciseViewModel exerciseViewModel = (ExerciseViewModel) userObject;
      append(exerciseViewModel.getPresentableName());
      if (!exerciseViewModel.getStatusText().trim().isEmpty()) {
        append(" [" + exerciseViewModel.getStatusText() + "]", STATUS_TEXT_STYLE);
      }
      setEnabled(exerciseViewModel.isSubmittable());
      setToolTipText(exerciseViewModel.isSubmittable()
          ? "Use the upload button to submit an exercise"
          : "This exercise cannot be submitted from the IDE");
      setIcon(statusToIcon(exerciseViewModel.getStatus()));
    } else if (userObject instanceof ExerciseGroupViewModel) {
      setIcon(PluginIcons.A_PLUS_EXERCISE_GROUP);
      ExerciseGroupViewModel groupViewModel = (ExerciseGroupViewModel) userObject;
      append(groupViewModel.getPresentableName());
      setEnabled(true);
      setIcon(PluginIcons.A_PLUS_EXERCISE_GROUP);
      setToolTipText("");
    } else if (userObject instanceof SubmissionResultViewModel) {
      SubmissionResultViewModel submissionResultViewModel = (SubmissionResultViewModel) userObject;
      setEnabled(true);
      append(submissionResultViewModel.getPresentableName());
      append(" [" + submissionResultViewModel.getStatusText() + "]", STATUS_TEXT_STYLE);
      setToolTipText("Double-click the submission to open it in the browser");
    }
  }

}
