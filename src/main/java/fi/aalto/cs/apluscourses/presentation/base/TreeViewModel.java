package fi.aalto.cs.apluscourses.presentation.base;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface TreeViewModel {
  @NotNull
  List<? extends TreeViewModel> getChildren();
}
