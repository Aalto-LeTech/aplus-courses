package fi.aalto.cs.apluscourses.ui.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;
import javax.swing.tree.TreeModel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TreeModelBuilderTest {

  private class UserObject {
    private final UserObject[] children;

    UserObject(UserObject... children) {
      this.children = children;
    }

    Stream<UserObject> streamChildren() {
      return Stream.of(children);
    }
  }

  @Test
  void testBuild() {
    TreeModelBuilder<UserObject> builder = new TreeModelBuilder<>() {
      @Override
      protected @NotNull
      Stream<UserObject> childrenOf(@NotNull UserObject obj) {
        return obj.streamChildren();
      }
    };

    UserObject child10 = new UserObject();
    UserObject child11 = new UserObject();

    UserObject child0 = new UserObject();
    UserObject child1 = new UserObject(child10, child11);
    UserObject child2 = new UserObject();

    UserObject root = new UserObject(child0, child1, child2);

    TreeModel treeModel = builder.build(root);

    Object rootNode = treeModel.getRoot();
    Assertions.assertSame(root, builder.getUserObject(rootNode));
    Assertions.assertEquals(3, treeModel.getChildCount(rootNode));

    Object childNode0 = treeModel.getChild(rootNode, 0);
    Assertions.assertSame(child0, builder.getUserObject(childNode0));
    Assertions.assertTrue(treeModel.isLeaf(childNode0));

    Object childNode1 = treeModel.getChild(rootNode, 1);
    Assertions.assertSame(child1, builder.getUserObject(childNode1));
    Assertions.assertEquals(2, treeModel.getChildCount(childNode1));

    Object childNode10 = treeModel.getChild(childNode1, 0);
    Assertions.assertSame(child10, builder.getUserObject(childNode10));
    Assertions.assertTrue(treeModel.isLeaf(childNode10));

    Object childNode11 = treeModel.getChild(childNode1, 1);
    Assertions.assertSame(child11, builder.getUserObject(childNode11));
    Assertions.assertTrue(treeModel.isLeaf(childNode11));

    Object childNode2 = treeModel.getChild(rootNode, 2);
    Assertions.assertSame(child2, builder.getUserObject(childNode2));
    Assertions.assertTrue(treeModel.isLeaf(childNode2));
  }
}
