package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleOrderEntry;
import com.intellij.openapi.roots.RootPolicy;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ListDependenciesPolicy extends RootPolicy<List<String>> {

  @Override
  public List<String> visitModuleOrderEntry(@NotNull ModuleOrderEntry moduleOrderEntry,
                                            List<String> value) {
    value.add(moduleOrderEntry.getModuleName());
    return value;
  }

  @Override
  public List<String> visitLibraryOrderEntry(@NotNull LibraryOrderEntry libraryOrderEntry,
                                             List<String> value) {
    if (!libraryOrderEntry.isModuleLevel()) {
      value.add(libraryOrderEntry.getLibraryName());
    }
    return value;
  }
}
