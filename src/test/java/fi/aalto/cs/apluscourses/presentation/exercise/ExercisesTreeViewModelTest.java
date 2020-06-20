package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import org.junit.Assert;
import org.junit.Test;

public class ExercisesTreeViewModelTest {

  @Test
  public void testEmptyTreeModel() {
    ExercisesTreeViewModel viewModel = new ExercisesTreeViewModel(Collections.emptyList());
    TreeModel treeModel = viewModel.toTreeModel();

    Assert.assertNull(treeModel.getRoot());
  }

  @Test
  public void testToTreeModel() {
    String message = "The tree model has the correct structure";

    Exercise exercise = new Exercise(123, "Test Exercise");
    ExerciseGroup group = new ExerciseGroup("Test Group", Arrays.asList(exercise));
    ExercisesTreeViewModel viewModel = new ExercisesTreeViewModel(Arrays.asList(group));

    TreeModel treeModel = viewModel.toTreeModel();

    Object root = treeModel.getRoot();
    Assert.assertEquals(message, 1, treeModel.getChildCount(root));

    Object groupNode = treeModel.getChild(root, 0);
    Object groupObject = ((DefaultMutableTreeNode) groupNode).getUserObject();
    Assert.assertEquals(message, "Test Group",
        ((ExerciseGroupViewModel) groupObject).getPresentableName());

    Object exerciseNode = treeModel.getChild(groupNode, 0);
    Object exerciseObject = ((DefaultMutableTreeNode) exerciseNode).getUserObject();
    Assert.assertEquals(message, "Test Exercise",
        ((ExerciseViewModel) exerciseObject).getPresentableName());
  }

}
