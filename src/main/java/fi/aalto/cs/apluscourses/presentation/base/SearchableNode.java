package fi.aalto.cs.apluscourses.presentation.base;

import org.jetbrains.annotations.NotNull;

public interface SearchableNode {
  @NotNull
  default String getSearchableString() {
    return "";
  }
}
