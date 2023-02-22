package fi.aalto.cs.apluscourses.intellij.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleOrderEntry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ListDependenciesPolicyTest {

  @Test
  void testVisitModuleOrderEntry() {
    String moduleName = "myModule";
    ModuleOrderEntry moduleOrderEntry = mock(ModuleOrderEntry.class);
    when(moduleOrderEntry.getModuleName()).thenReturn(moduleName);

    ListDependenciesPolicy policy = new ListDependenciesPolicy();
    List<String> value = new ArrayList<>();
    policy.visitModuleOrderEntry(moduleOrderEntry, value);

    Assertions.assertTrue(value.contains(moduleName));
  }

  @Test
  void testVisitLibraryOrderEntry() {
    String projectLibraryName = "projectLib";
    LibraryOrderEntry projectLibraryOrderEntry = mock(LibraryOrderEntry.class);
    when(projectLibraryOrderEntry.getLibraryName()).thenReturn(projectLibraryName);
    when(projectLibraryOrderEntry.isModuleLevel()).thenReturn(false);

    String localLibraryName = "localLib";
    LibraryOrderEntry localLibraryOrderEntry = mock(LibraryOrderEntry.class);
    when(localLibraryOrderEntry.getLibraryName()).thenReturn(localLibraryName);
    when(localLibraryOrderEntry.isModuleLevel()).thenReturn(true);

    ListDependenciesPolicy policy = new ListDependenciesPolicy();
    List<String> value = new ArrayList<>();
    policy.visitLibraryOrderEntry(projectLibraryOrderEntry, value);
    policy.visitLibraryOrderEntry(localLibraryOrderEntry, value);

    Assertions.assertTrue(value.contains(projectLibraryName));
    Assertions.assertFalse(value.contains(localLibraryName));
  }
}
