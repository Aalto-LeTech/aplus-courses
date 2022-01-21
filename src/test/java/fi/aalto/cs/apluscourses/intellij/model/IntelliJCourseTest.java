package fi.aalto.cs.apluscourses.intellij.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IntelliJCourseTest {

  @Test
  void testCreateIntelliJCourse() {
    String id = "id";
    String name = "testName";
    APlusProject project = mock(APlusProject.class);
    doReturn(Paths.get("")).when(project).getBasePath();
    when(project.getMessageBus()).thenReturn(mock(MessageBus.class));
    CommonLibraryProvider commonLibraryProvider = new CommonLibraryProvider(project);
    IntelliJCourse course = new IntelliJCourse(id, name,
        "http://localhost:1355",
        Collections.emptyList(),
        // modules
        Collections.emptyList(),
        // libraries
        Collections.emptyList(),
        // exerciseModules
        Collections.emptyMap(),
        // resourceUrls
        Collections.emptyMap(),
        // vmOptions
        Collections.emptyMap(),
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        project,
        commonLibraryProvider,
        Collections.emptyMap());
    Assertions.assertEquals(id, course.getId());
    Assertions.assertEquals(name, course.getName());
    Assertions.assertSame(project, course.getProject());
    Assertions.assertSame(commonLibraryProvider, course.getCommonLibraryProvider());
  }

  @Test
  void testGetComponents() {
    String moduleName = "testModule";
    Module module = new ModelExtensions.TestModule(moduleName);
    List<Module> modules = new ArrayList<>();
    modules.add(module);

    String libraryName = "library";
    Library library = new ModelExtensions.TestLibrary(libraryName);

    APlusProject project = mock(APlusProject.class);
    doReturn(Paths.get("")).when(project).getBasePath();
    CommonLibraryProvider commonLibraryProvider = new CommonLibraryProvider(project) {
      @Nullable
      @Override
      protected Library createLibrary(@NotNull String name) {
        return name.equals(libraryName) ? library : null;
      }
    };

    IntelliJCourse course = new IntelliJCourse("cool id",
        "testProject",
        "https://example.com",
        Collections.emptyList(),
        modules,
        // libraries
        Collections.emptyList(),
        // exerciseModules
        Collections.emptyMap(),
        // resourceUrls
        Collections.emptyMap(),
        // vmOptions
        Collections.emptyMap(),
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        project,
        commonLibraryProvider,
        Collections.emptyMap());

    Collection<Component> components1 = course.getComponents();
    Assertions.assertEquals(1, components1.size());
    Assertions.assertTrue(components1.contains(module));

    Assertions.assertSame(module, course.getComponentIfExists(moduleName));
    Assertions.assertSame(library, course.getComponentIfExists(libraryName));
    Assertions.assertNull(course.getComponentIfExists("otherComponent"));

    Collection<Component> components2 = course.getComponents();
    Assertions.assertEquals(2, components2.size());
    Assertions.assertTrue(components2.contains(module));
    Assertions.assertTrue(components2.contains(library));
  }

  @Test
  void testGetComponentIfExists() {
    var project = mock(APlusProject.class);
    doReturn(Paths.get("")).when(project).getBasePath();

    String moduleName = "moominModule";
    VirtualFile file = mock(VirtualFile.class);
    when(file.getName()).thenReturn(moduleName);
    when(file.getPath()).thenReturn(Paths.get(moduleName).toAbsolutePath().toString());

    Module module = new ModelExtensions.TestModule(moduleName);
    List<Module> modules = new ArrayList<>();
    modules.add(module);

    IntelliJCourse course = new IntelliJCourse("courseId",
        "testtesttest",
        "http://localhost:2000",
        Collections.emptyList(),
        modules,
        // libraries
        Collections.emptyList(),
        // exerciseModules
        Collections.emptyMap(),
        // resourceUrls
        Collections.emptyMap(),
        // vmOptions
        Collections.emptyMap(),
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        project,
        mock(CommonLibraryProvider.class),
        Collections.emptyMap());

    Assertions.assertSame(module, course.getComponentIfExists(file));
  }

  @Test
  void testGetComponentIfExistsReturnsNull() {
    var project = mock(APlusProject.class);
    doReturn(Paths.get("")).when(project).getBasePath();

    VirtualFile file1 = mock(VirtualFile.class);
    when(file1.getName()).thenReturn("someFile");
    when(file1.getPath()).thenReturn("somePath");

    String moduleName = "ohBoyModule";
    VirtualFile file2 = mock(VirtualFile.class);
    when(file2.getName()).thenReturn(moduleName);
    when(file2.getPath()).thenReturn("someOtherPath");

    IntelliJCourse course = new IntelliJCourse("testId",
        "testProject",
        "http://localhost:2200",
        Collections.emptyList(),
        Stream.of(new ModelExtensions.TestModule(moduleName)).collect(Collectors.toList()),
        // libraries
        Collections.emptyList(),
        // exerciseModules
        Collections.emptyMap(),
        // resourceUrls
        Collections.emptyMap(),
        // vmOptions
        Collections.emptyMap(),
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        project,
        mock(CommonLibraryProvider.class),
        Collections.emptyMap());

    Assertions.assertNull(course.getComponentIfExists(file1));
    Assertions.assertNull(course.getComponentIfExists(file2));
  }
}
