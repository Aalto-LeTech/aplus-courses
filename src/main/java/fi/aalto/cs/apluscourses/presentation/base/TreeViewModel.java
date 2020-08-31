package fi.aalto.cs.apluscourses.presentation.base;

import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface TreeViewModel {
  @NotNull
  List<? extends TreeViewModel> getChildren();
}
