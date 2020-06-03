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
  public void testGetCourseFilePath() {
    Project project = mock(Project.class);
    doReturn("test").when(project).getBasePath();

    APlusProject aplusProject = new APlusProject(project);

    Assert.assertEquals("The course file path should be correct",
        Paths.get("test", Project.DIRECTORY_STORE_FOLDER, "a-plus-project.json"),
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

  @Test
  public void testAddCourseFileEntry() throws IOException, InterruptedException {
    final int numThreads = 16;

    Project project = mock(Project.class);
    APlusProject aplusProject = new APlusProject(project);

    File temp = FileUtilRt.createTempFile("test-course-file", "json", true);
    FileUtils.writeStringToFile(temp, "{}", StandardCharsets.UTF_8);

    URL url = new URL("http://localhost:8000");

    List<Thread> threads = new ArrayList<>(numThreads);
    AtomicBoolean failed = new AtomicBoolean(false);
    for (int i = 0; i < numThreads; ++i) {
      IntelliJModule module = new IntelliJModule("name" + i, url, "id" + i,
          aplusProject);
      Runnable runnable = () -> {
        try {
          aplusProject.addCourseFileEntry(temp, module);
        } catch (IOException e) {
          failed.set(true);
        }
      };
      threads.add(new Thread(runnable));
    }

    threads.forEach(Thread::start);

    for (Thread thread : threads) {
      thread.join();
    }

    if (failed.get()) {
      Assert.fail("IOException thrown from APlusProject#addCourseFileEntry");
    }

    JSONObject jsonObject
        = new JSONObject(FileUtils.readFileToString(temp, StandardCharsets.UTF_8));
    JSONObject modulesObject = jsonObject.getJSONObject("modules");
    Set<String> keys = modulesObject.keySet();
    Assert.assertEquals("The file should contain the correct number of module entries",
        numThreads, keys.size());
    for (String key : keys) {
      // This will throw and fail this test if the JSON format in the file is malformed
      modulesObject.getJSONObject(key).getString("id");
    }
  }

  @Test
  public void testGetCourseFileModuleIds() throws IOException, InterruptedException {
    Project project = mock(Project.class);
    File temp = FileUtilRt.createTempFile("course-file", "json", true);
    APlusProject aplusProject = new APlusProject(project) {
      @NotNull
      @Override
      public Path getCourseFilePath() {
        return temp.toPath();
      }
    };

    final int numThreads = 16;
    List<Thread> threads = new ArrayList<>(numThreads);
    AtomicBoolean failed = new AtomicBoolean(false);

    JSONObject modulesObject = new JSONObject();
    for (int i = 0; i < numThreads; ++i) {
      String moduleName = "m" + i;
      String moduleId = "version" + i;
      modulesObject.put(moduleName, new JSONObject().put("id", moduleId));

      threads.add(new Thread(() -> {
        try {
          Map<String, IntelliJModuleMetadata> moduleIds = aplusProject
              .getCourseFileModuleMetadata();
          failed.set(!moduleId.equals(moduleIds.get(moduleName).getModuleId()));
        } catch (IOException e) {
          failed.set(true);
        }
      }));
    }

    JSONObject courseFileJson = new JSONObject().put("modules", modulesObject);
    FileUtils.writeStringToFile(temp, courseFileJson.toString(), StandardCharsets.UTF_8);

    threads.forEach(Thread::start);
    for (Thread thread : threads) {
      thread.join();
    }

    if (failed.get()) {
      Assert.fail("APlusProject#getCourseFileModuleIds did not return the correct map");
    }
  }
}
