package fi.aalto.cs.apluscourses.ui.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.Nullable;

public abstract class TreePathEncoder<T> {
  private final ConcurrentMap<TreePath, T> map = new ConcurrentHashMap<>();

  public T encode(@Nullable TreePath treePath) {
    return treePath == null ? emptyCode()
        : map.computeIfAbsent(treePath, this::encodeInternal);
  }

  private T encodeInternal(TreePath treePath) {
    return encodeNode(encode(treePath.getParentPath()), treePath.getLastPathComponent());
  }

  protected abstract T emptyCode();

  protected abstract T encodeNode(T parentCode, Object node);
}
