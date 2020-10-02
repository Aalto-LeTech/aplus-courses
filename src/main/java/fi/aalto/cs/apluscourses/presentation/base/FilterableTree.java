package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.utils.Tree;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface FilterableTree extends Tree {
  boolean isVisible();

  @Override
  @NotNull
  List<? extends FilterableTree> getChildren();

  @NotNull
  default Stream<? extends FilterableTree> streamVisibleChildren() {
    return getChildren().stream().filter(FilterableTree::isVisible);
  }
}
