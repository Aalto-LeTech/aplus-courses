package fi.aalto.cs.apluscourses.presentation.base;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public interface TreeViewModel {
  @Nullable
  List<? extends TreeViewModel> getSubtrees();
}
