package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import icons.PluginIcons;
import javax.swing.Icon;
import javax.swing.JTree;
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
    SelectableNodeViewModel<?> viewModel = TreeView.getViewModel(value);
    if (viewModel instanceof ExerciseViewModel) {
      ExerciseViewModel exerciseViewModel = (ExerciseViewModel) viewModel;
      append(exerciseViewModel.getPresentableName(), SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
      if (!exerciseViewModel.getStatusText().trim().isEmpty()) {
        append(" [" + exerciseViewModel.getStatusText() + "]", STATUS_TEXT_STYLE);
      }
      setEnabled(exerciseViewModel.isSubmittable());
      setToolTipText(exerciseViewModel.isSubmittable()
          ? "Use the upload button to submit an exercise"
          : "This exercise cannot be submitted from the IDE");
      setIcon(statusToIcon(exerciseViewModel.getStatus()));
    } else if (viewModel instanceof ExerciseGroupViewModel) {
      ExerciseGroupViewModel groupViewModel = (ExerciseGroupViewModel) viewModel;
      append("", SimpleTextAttributes.REGULAR_ATTRIBUTES, true); // disable search highlighting
      append(groupViewModel.getPresentableName(), SimpleTextAttributes.REGULAR_ATTRIBUTES, false);
      setEnabled(true);
      if (groupViewModel.getModel().isOpen()) {
        setIcon(PluginIcons.A_PLUS_EXERCISE_GROUP);
      } else {
        setIcon(PluginIcons.A_PLUS_EXERCISE_GROUP_CLOSED);
      }
      setToolTipText("");
    } else if (viewModel instanceof SubmissionResultViewModel) {
      SubmissionResultViewModel resultViewModel = (SubmissionResultViewModel) viewModel;
      setEnabled(true);
      append("", SimpleTextAttributes.REGULAR_ATTRIBUTES, true); // disable search highlighting
      append(resultViewModel.getPresentableName(), SimpleTextAttributes.REGULAR_ATTRIBUTES, false);
      append(" [" + resultViewModel.getStatusText() + "]", STATUS_TEXT_STYLE, false);
      setToolTipText("Double-click the submission to open it in the browser");
    }
  }
}
