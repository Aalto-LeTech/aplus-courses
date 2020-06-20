package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.jetbrains.annotations.NotNull;

public class ExercisesTreeViewModel {

  private final List<ExerciseGroupViewModel> groupViewModels;

  /**
   * Construct an exercises tree view model from the given exercise groups.
   */
  public ExercisesTreeViewModel(@NotNull List<ExerciseGroup> exerciseGroups) {
    this.groupViewModels = exerciseGroups
        .stream()
        .map(ExerciseGroupViewModel::new)
        .collect(Collectors.toList());
  }

  /**
   * Construct a {@link TreeModel} instance out of this view model.
   */
  @NotNull
  public TreeModel toTreeModel() {
    if (groupViewModels.isEmpty()) {
      return new DefaultTreeModel(null);
    }

    DefaultMutableTreeNode root = new DefaultMutableTreeNode(null);
    groupViewModels.forEach(groupViewModel -> {
      DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(groupViewModel);
      groupViewModel
          .getExerciseViewModels()
          .forEach(exerciseViewModel -> {
            groupNode.add(
                new DefaultMutableTreeNode(exerciseViewModel, false)
            );
          });
      root.add(groupNode);
    });
    return new DefaultTreeModel(root);
  }

}
