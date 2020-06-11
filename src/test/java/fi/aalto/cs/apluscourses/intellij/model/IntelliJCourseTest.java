package fi.aalto.cs.apluscourses.intellij.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class IntelliJCourseTest {

  @Test
  public void testCreateIntelliJCourse() {
    String name = "testName";
    APlusProject project = mock(APlusProject.class);
    CommonLibraryProvider commonLibraryProvider = new CommonLibraryProvider(project);
    IntelliJCourse course = new IntelliJCourse(name,
        Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(),
        Collections.emptyMap(), Collections.emptyList(), project, commonLibraryProvider);
    assertEquals(name, course.getName());
    assertSame(project, course.getProject());
    assertSame(commonLibraryProvider, course.getCommonLibraryProvider());
  }

  @Test
  public void testGetComponents() {
    String moduleName = "testModule";
    Module module = new ModelExtensions.TestModule(moduleName);
    List<Module> modules = new ArrayList<>();
    modules.add(module);

    String libraryName = "library";
    Library library = new ModelExtensions.TestLibrary(libraryName);

    APlusProject project = mock(APlusProject.class);
    CommonLibraryProvider commonLibraryProvider = new CommonLibraryProvider(project) {
      @Nullable
      @Override
      protected Library createLibrary(@NotNull String name) {
        return name.equals(libraryName) ? library : null;
      }
    };

    IntelliJCourse course = new IntelliJCourse("testProject",
        modules, Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(),
        Collections.emptyList(), project, commonLibraryProvider);

    Collection<Component> components1 = course.getComponents();
    assertEquals(1, components1.size());
    assertTrue(components1.contains(module));

    assertSame(module, course.getComponentIfExists(moduleName));
    assertSame(library, course.getComponentIfExists(libraryName));
    assertNull(course.getComponentIfExists("otherComponent"));

    Collection<Component> components2 = course.getComponents();
    assertEquals(2, components2.size());
    assertTrue(components2.contains(module));
    assertTrue(components2.contains(library));
  }

  @Test
  public void testGetComponentIfExists() {
    String moduleName = "moominModule";
    VirtualFile file = mock(VirtualFile.class);
    when(file.getName()).thenReturn(moduleName);
    when(file.getPath()).thenReturn(Paths.get(moduleName).toAbsolutePath().toString());

    Module module = new ModelExtensions.TestModule(moduleName);
    List<Module> modules = new ArrayList<>();
    modules.add(module);

    IntelliJCourse course = new IntelliJCourse("testProject",
        modules, Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(),
        Collections.emptyList(), mock(APlusProject.class), mock(CommonLibraryProvider.class));

    assertSame(module, course.getComponentIfExists(file));
  }

  @Test
  public void testGetComponentIfExistsReturnsNull() {
    VirtualFile file1 = mock(VirtualFile.class);
    when(file1.getName()).thenReturn("someFile");
    when(file1.getPath()).thenReturn("somePath");

    String moduleName = "ohBoyModule";
    VirtualFile file2 = mock(VirtualFile.class);
    when(file2.getName()).thenReturn(moduleName);
    when(file2.getPath()).thenReturn("someOtherPath");

    IntelliJCourse course = new IntelliJCourse("testProject",
        Stream.of(new ModelExtensions.TestModule(moduleName)).collect(Collectors.toList()),
        Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(),
        Collections.emptyList(), mock(APlusProject.class), mock(CommonLibraryProvider.class));

    assertNull(course.getComponentIfExists(file1));
    assertNull(course.getComponentIfExists(file2));
  }
}
