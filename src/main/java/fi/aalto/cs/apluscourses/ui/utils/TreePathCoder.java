package fi.aalto.cs.apluscourses.ui.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.Nullable;

public abstract class TreePathCoder<T> {
  private final ConcurrentMap<TreePath, T> map = new ConcurrentHashMap<>();

  public T code(@Nullable TreePath treePath) {
    return treePath == null ? emptyCode()
        : map.computeIfAbsent(treePath, this::codeInternal);
  }

  private T codeInternal(TreePath treePath) {
    return codeNode(code(treePath.getParentPath()), treePath.getLastPathComponent());
  }

  protected abstract T emptyCode();

  protected abstract T codeNode(T parentCode, Object node);
}
