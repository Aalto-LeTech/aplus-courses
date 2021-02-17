package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleOrderEntry;
import com.intellij.openapi.roots.RootPolicy;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class is a {@link RootPolicy} that builds a list of the names of those
 * {@link com.intellij.openapi.roots.OrderEntry} objects that represents dependencies of an
 * {@link fi.aalto.cs.apluscourses.intellij.model.IntelliJComponent} object (that is, modules
 * and non-module-level libraries).
 */
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
