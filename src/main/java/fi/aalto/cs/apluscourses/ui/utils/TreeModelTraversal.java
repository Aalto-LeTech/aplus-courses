package fi.aalto.cs.apluscourses.ui.utils;

import fi.aalto.cs.apluscourses.utils.Streamable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.NotNull;

public class TreeModelTraversal {
  private final TreeModel treeModel;

  public TreeModelTraversal(TreeModel treeModel) {
    this.treeModel = treeModel;
  }

  public Stream<TreePath> traverse() {
    return traverse(new TreePath(treeModel.getRoot()));
  }

  private Stream<TreePath> traverse(TreePath treePath) {
    return Stream.concat(Stream.of(treePath), // pre-order guaranteed
        getChildNodeStream(treePath.getLastPathComponent())
        .map(treePath::pathByAddingChild)
        .flatMap(this::traverse));
  }

  public Stream<Object> getChildNodeStream(Object node) {
    return ((Streamable<Object>) () -> new NodeIterator(node)).stream();
  }

  private class NodeIterator implements Iterator<Object> {
    private final Object node;
    private int index = 0;
    private final int childCount;

    public NodeIterator(@NotNull Object node) {
      this.node = node;
      childCount = treeModel.getChildCount(node);
    }

    @Override
    public boolean hasNext() {
      return index < childCount;
    }

    @Override
    public Object next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return treeModel.getChild(node, index++);
    }
  }
}
