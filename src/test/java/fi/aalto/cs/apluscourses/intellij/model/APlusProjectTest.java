package fi.aalto.cs.apluscourses.intellij.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Component;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class APlusProjectTest {

  @Test
  public void testGetCourseFilePath() {
    Project project = mock(Project.class);
    doReturn("test").when(project).getBasePath();

    APlusProject aplusProject = new APlusProject(project);

    Assert.assertEquals("The course file path should be correct",
        Paths.get("test", Project.DIRECTORY_STORE_FOLDER, "a-plus-project"),
        aplusProject.getCourseFilePath());
  }

  @Test
  public void testGetBasePath() {
    Project project = mock(Project.class);
    doReturn(".idea").when(project).getBasePath();

    APlusProject aplusProject = new APlusProject(project);

    Assert.assertEquals("The base path should be correct",
        Paths.get(Project.DIRECTORY_STORE_FOLDER),
        aplusProject.getBasePath());
  }

  @Test
  public void testResolveComponentState() {
    final String loadedComponentName = "loadedModule";
    final String fetchedComponentName = "fetchedModule";
    final String notInstalledComponentName = "notInstalledModule";
    final String errorComponentName = "errorModule";

    APlusProject project = new APlusProject(mock(Project.class)) {
      @Override
      public boolean doesDirExist(@NotNull Path relativePath) {
        String pathStr = relativePath.toString();
        return pathStr.equals(loadedComponentName) || pathStr.equals(fetchedComponentName);
      }
    };

    IntelliJModelExtensions.TestComponent loadedComponent =
        new IntelliJModelExtensions.TestComponent(loadedComponentName, new Object());
    assertEquals(Component.LOADED, project.resolveComponentState(loadedComponent));

    IntelliJModelExtensions.TestComponent fetchedComponent =
        new IntelliJModelExtensions.TestComponent(fetchedComponentName, null);
    assertEquals(Component.FETCHED, project.resolveComponentState(fetchedComponent));

    IntelliJModelExtensions.TestComponent notInstalledComponent =
        new IntelliJModelExtensions.TestComponent(notInstalledComponentName, null);
    assertEquals(Component.NOT_INSTALLED, project.resolveComponentState(notInstalledComponent));

    IntelliJModelExtensions.TestComponent errorComponent =
        new IntelliJModelExtensions.TestComponent(errorComponentName, new Object());
    assertEquals(Component.ERROR, project.resolveComponentState(errorComponent));
  }
}
