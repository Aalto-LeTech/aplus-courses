package fi.aalto.cs.apluscourses.intellij.model;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.model.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class APlusProjectTest {

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
    Assert.assertEquals(Component.LOADED, project.resolveComponentState(loadedComponent));

    IntelliJModelExtensions.TestComponent fetchedComponent =
        new IntelliJModelExtensions.TestComponent(fetchedComponentName, null);
    Assert.assertEquals(Component.FETCHED, project.resolveComponentState(fetchedComponent));

    IntelliJModelExtensions.TestComponent notInstalledComponent =
        new IntelliJModelExtensions.TestComponent(notInstalledComponentName, null);
    Assert.assertEquals(Component.NOT_INSTALLED,
        project.resolveComponentState(notInstalledComponent));

    IntelliJModelExtensions.TestComponent errorComponent =
        new IntelliJModelExtensions.TestComponent(errorComponentName, new Object());
    Assert.assertEquals(Component.ERROR, project.resolveComponentState(errorComponent));
  }

}
