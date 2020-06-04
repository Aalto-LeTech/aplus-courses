package fi.aalto.cs.apluscourses.intellij.utils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModuleMetadata;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class CourseFileManagerTest {

  @Test
  public void testAddEntryForModule() throws IOException, InterruptedException {
    final int numThreads = 16;

    // We have to build the correct directory structure here:
    // <temp directory>/.idea/a-plus-project.json
    Project project = mock(Project.class);
    File tempDir = FileUtilRt.createTempDirectory("test1", "", true);
    doReturn(tempDir.toString()).when(project).getBasePath();
    FileUtilRt.createTempDirectory(tempDir, Project.DIRECTORY_STORE_FOLDER, "", true);

    URL url = new URL("http://localhost:8000");
    CourseFileManager.getInstance().createAndLoad(project, url);

    ModelFactory modelFactory = new ModelExtensions.TestModelFactory();
    List<Thread> threads = new ArrayList<>(numThreads);
    AtomicBoolean failed = new AtomicBoolean(false);
    for (int i = 0; i < numThreads; ++i) {
      Module module = modelFactory.createModule("name" + i, url, "id" + i);
      Runnable runnable = () -> {
        try {
          CourseFileManager.getInstance().addEntryForModule(module);
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
      Assert.fail("IOException thrown from CourseFileManager#addEntryForModule");
    }

    File courseFile = Paths
        .get(tempDir.toString(), Project.DIRECTORY_STORE_FOLDER, "a-plus-project.json")
        .toFile();
    JSONObject jsonObject
        = new JSONObject(FileUtils.readFileToString(courseFile, StandardCharsets.UTF_8));
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
  public void testGetModulesMetadata() throws IOException, InterruptedException {
    final int numThreads = 16;

    // We have to build the correct directory structure here:
    // <temp directory>/.idea/a-plus-project.json
    Project project = mock(Project.class);
    File tempDir = FileUtilRt.createTempDirectory("test2", "", true);
    doReturn(tempDir.toString()).when(project).getBasePath();
    FileUtilRt.createTempDirectory(tempDir, Project.DIRECTORY_STORE_FOLDER, "", true);

    List<Thread> threads = new ArrayList<>(numThreads);
    AtomicBoolean failed = new AtomicBoolean(false);
    JSONObject modulesObject = new JSONObject();
    for (int i = 0; i < numThreads; ++i) {
      String moduleName = "m" + i;
      String moduleId = "version" + i;
      ZonedDateTime downloadedAt = ZonedDateTime.now();
      JSONObject moduleObject = new JSONObject();
      moduleObject.put("id", moduleId);
      moduleObject.put("downloadedAt", downloadedAt);
      modulesObject.put(moduleName, moduleObject);

      threads.add(new Thread(() -> {
        Map<String, IntelliJModuleMetadata> moduleIds
            = CourseFileManager.getInstance().getModulesMetadata();
        failed.set(!moduleId.equals(moduleIds.get(moduleName).getModuleId()));
      }));
    }


    JSONObject courseFileJson = new JSONObject()
        .put("modules", modulesObject)
        .put("url", new URL("https://example.com"));
    File courseFile = Paths
        .get(tempDir.toString(), Project.DIRECTORY_STORE_FOLDER, "a-plus-project.json")
        .toFile();

    FileUtils.writeStringToFile(courseFile, courseFileJson.toString(), StandardCharsets.UTF_8);

    CourseFileManager.getInstance().load(project);

    threads.forEach(Thread::start);
    for (Thread thread : threads) {
      thread.join();
    }

    if (failed.get()) {
      Assert.fail("CourseFileManager#getModulesMetadata did not return the correct map");
    }
  }
}
