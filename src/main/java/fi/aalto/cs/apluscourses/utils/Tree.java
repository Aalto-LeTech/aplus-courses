package fi.aalto.cs.apluscourses.utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Tree {
  @NotNull
  List<? extends Tree> getChildren();

  /**
   * Traverses the tree in depth-first pre-order and finds the first node that satisfies the
   * {@code selector}.  Returns the path to that node as a list whose first item is this node and
   * last is the found node.
   *
   * @param selector A predicate that we use to test nodes.
   * @param <T>      Subtype of {@link Tree} that each node in the tree must be instances of.
   * @return A path to the node as a list or null, if no node matches {@code selector}.
   */
  @Nullable
  @SuppressWarnings("unchecked")
  default <T extends Tree> List<T> traverseAndFind(@NotNull Predicate<T> selector) {
    T root = (T) this;
    if (selector.test(root)) {
      return Collections.singletonList(root);
    }
    return getChildren()
        .stream()
        .map(child -> child.traverseAndFind(selector))
        .filter(Objects::nonNull)
        .findFirst()
        .map(path -> new ConsList<>(root, path))
        .orElse(null);
  }
}
