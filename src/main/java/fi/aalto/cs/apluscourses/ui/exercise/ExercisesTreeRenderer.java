package fi.aalto.cs.apluscourses.ui.exercise;

import com.intellij.ui.ColoredTreeCellRenderer;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jetbrains.annotations.NotNull;

public class ExercisesTreeRenderer extends ColoredTreeCellRenderer {

  @Override
  public void customizeCellRenderer(@NotNull JTree tree,
                                    Object value,
                                    boolean isSelected,
                                    boolean isExpanded,
                                    boolean isLeaf,
                                    int row,
                                    boolean hasFocus) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    if (node.getUserObject() instanceof ExercisesTreeViewModel) {
      // This is the root node, which is hidden anyways.
      return;
    }

    if (isLeaf) {
      // TODO: set custom exercise icon here using setIcon
      ExerciseViewModel exerciseViewModel = (ExerciseViewModel) node.getUserObject();
      append(exerciseViewModel.getPresentableName());
    } else {
      ExerciseGroupViewModel groupViewModel = (ExerciseGroupViewModel) node.getUserObject();
      append(groupViewModel.getPresentableName());
    }
  }

}
