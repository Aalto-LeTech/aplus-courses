package fi.aalto.cs.apluscourses.presentation.filter;

import org.jetbrains.annotations.NotNull;

public interface Filterable {
  void applyFilter(@NotNull Filter filter) throws InterruptedException;
}
