package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.presentation.base.FilterableTree;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreeNode;
import jdk.internal.jline.internal.Nullable;
import org.jetbrains.annotations.NotNull;

public class CompositeTreeNode implements TreeNode {

  @Nullable
  private final CompositeTreeNode parent;
  @NotNull
  private final List<CompositeTreeNode> children;

  @NotNull
  private final Object userObject;

  public static CompositeTreeNode create(@NotNull FilterableTree userObject) {
    return new CompositeTreeNode(userObject, null);
  }

  private CompositeTreeNode(@NotNull FilterableTree userObject,
                            @Nullable CompositeTreeNode parent) {
    this.userObject = userObject;
    this.parent = parent;
    children = userObject
        .streamVisibleChildren()
        .map(this::createChild) // warning: passing non-fully-constructed this
        .collect(Collectors.toList());
  }

  private CompositeTreeNode createChild(FilterableTree userObject) {
    return new CompositeTreeNode(userObject, this);
  }

  @NotNull
  public Object getUserObject() {
    return userObject;
  }

  @Override
  public CompositeTreeNode getChildAt(int childIndex) {
    return children.get(childIndex);
  }

  @Override
  public int getChildCount() {
    return children.size();
  }

  @Override
  public CompositeTreeNode getParent() {
    return parent;
  }

  @Override
  public int getIndex(TreeNode node) {
    return node instanceof CompositeTreeNode ? children.indexOf(node) : -1;
  }

  @Override
  public boolean getAllowsChildren() {
    return !isLeaf();
  }

  @Override
  public boolean isLeaf() {
    return children.isEmpty();
  }

  @Override
  public Enumeration<? extends CompositeTreeNode> children() {
    return Collections.enumeration(children);
  }
}
