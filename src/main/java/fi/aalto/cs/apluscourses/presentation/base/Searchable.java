package fi.aalto.cs.apluscourses.presentation.base;

import org.jetbrains.annotations.NotNull;

public interface Searchable {
  @NotNull
  default String getSearchableString() {
    return "";
  }
}
