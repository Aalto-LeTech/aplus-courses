package fi.aalto.cs.apluscourses.ui.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.Nullable;

public abstract class TreePathEncoder<T> {

  // Note: ConcurrentHashMap doesn't work as a memoization map for recursive functions.
  // It might not be strictly necessary to use synchronized map here,
  // reconsider the issue if this some day becomes a performance bottleneck.
  private final Map<TreePath, T> memo = Collections.synchronizedMap(new HashMap<>());

  /**
   * Encodes the given tree path to a code of type T.
   *
   * @param treePath A tree path.
   * @return An object of type T that is a code for the given tree path.
   */
  public T encode(@Nullable TreePath treePath) {
    if (treePath == null) {
      return emptyCode();
    }
    T value = memo.get(treePath);
    if (value == null) {
      value = encodeInternal(treePath);
      // In parallel execution, it might happen that the value is calculated twice.
      // That doesn't, however, matter in the sense of correctness.
      memo.put(treePath, value);
    }
    return value;
  }

  private T encodeInternal(TreePath treePath) {
    return encodeNode(encode(treePath.getParentPath()), treePath.getLastPathComponent());
  }

  protected abstract T emptyCode();

  protected abstract T encodeNode(T parentCode, Object node);
}
