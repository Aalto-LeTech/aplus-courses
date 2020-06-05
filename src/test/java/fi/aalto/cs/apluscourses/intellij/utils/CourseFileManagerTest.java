package fi.aalto.cs.apluscourses.intellij.utils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModuleMetadata;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.ModelFactory;
import fi.aalto.cs.apluscourses.model.Module;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.util.parsing.json.JSON;

public class CourseFileManagerTest {

  // CourseFileManager is supposed to be thread safe, so all of these tests use multiple threads.
  private static final int numThreads = 16;

  private CourseFileManager manager;
  private Project project;
  private File courseFile;
  private List<Thread> threads;
  private AtomicBoolean threadFailed;

  @NotNull
  @Contract(" -> new")
  private JSONObject getCourseFileContents() throws IOException {
    return new JSONObject(FileUtils.readFileToString(courseFile, StandardCharsets.UTF_8));
  }


  private static void startAndJoinEach(@NotNull Collection<Thread> threads)
      throws InterruptedException {
    threads.forEach(Thread::start);
    for (Thread thread : threads) {
      thread.join();
    }
  }

  /**
   * Run before every test, initializes useful instance variables.
   */
  @Before
  public void initializeObjects() throws IOException {
    manager = CourseFileManager.getInstance();
    project = mock(Project.class);
    File tempDir = FileUtilRt.createTempDirectory("test", "", true);
    doReturn(tempDir.toString()).when(project).getBasePath();
    FileUtilRt.createTempDirectory(tempDir, Project.DIRECTORY_STORE_FOLDER, "", true);
    courseFile = Paths
        .get(tempDir.toString(), Project.DIRECTORY_STORE_FOLDER, "a-plus-project.json")
        .toFile();
    threads = new ArrayList<>(numThreads);
    threadFailed = new AtomicBoolean(false);
  }

  @Test
  public void testCreateAndLoad() throws IOException, InterruptedException {
    URL url = new URL("http://localhost:3000");

    for (int i = 0; i < numThreads; ++i) {
      threads.add(new Thread(() -> {
        try {
          manager.createAndLoad(project, url);
        } catch (IOException e) {
          threadFailed.set(true);
        }
      }));
    }

    startAndJoinEach(threads);

    Assert.assertFalse(threadFailed.get());

    Assert.assertEquals("CourseFileManager returns the correct url", url, manager.getCourseUrl());
    Assert.assertTrue("CourseFileManager returns an empty map for modules metadata",
        manager.getModulesMetadata().isEmpty());

    JSONObject jsonObject = getCourseFileContents();
    URL jsonUrl = new URL(jsonObject.getString("url"));
    Assert.assertEquals("The course file contains JSON with the given URL", url, jsonUrl);
  }

  @Test
  public void testCreateAndLoadWithExistingFile() throws IOException {
    URL url = new URL("https://google.com");

    JSONObject modulesObject = new JSONObject().put("awesome module",
        new JSONObject().put("id", "abc").put("downloadedAt", ZonedDateTime.now())
    );
    JSONObject jsonObject = new JSONObject().put("url", url).put("modules", modulesObject);

    FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);

    manager.createAndLoad(project, new URL("https://github.com"));

    Assert.assertEquals("CourseFileManager#createAndLoad gets the URL of the existing course file",
        url, manager.getCourseUrl());
    Assert.assertEquals("CourseFileManager#createAndLoad gets the existing modules metadata",
        1, manager.getModulesMetadata().size());
    Assert.assertEquals("CourseFileManager#createAndLoad gets the existing modules metadata",
        "abc", manager.getModulesMetadata().get("awesome module").getModuleId());
  }

  @Test
  public void testLoad() throws IOException, InterruptedException {
    JSONObject jsonObject = new JSONObject()
        .put("url", new URL("https://example.org"))
        .put("modules", new JSONObject()
            .put("great name", new JSONObject()
                .put("id", "123")
                .put("downloadedAt", ZonedDateTime.now()))
            .put("also a great name", new JSONObject()
                .put("id", "456")
                .put("downloadedAt", ZonedDateTime.now())));

    FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);

    for (int i = 0; i < numThreads; ++i) {
      threads.add(new Thread(() -> {
        try {
          // Load should return true
          threadFailed.compareAndSet(false, !manager.load(project));
        } catch (IOException e) {
          threadFailed.set(true);
        }
      }));
    }

    startAndJoinEach(threads);

    Assert.assertEquals("CourseFileManager returns the correct URL",
        new URL("https://example.com"), manager.getCourseUrl());
    Map<String, IntelliJModuleMetadata> metadata = manager.getModulesMetadata();
    Assert.assertEquals("CourseFileManager should return the correct number of modules metadata",
        2, metadata.size());
    Assert.assertEquals("CourseFileManager should return the correct modules metadata",
        "123", metadata.get("great name").getModuleId());
    Assert.assertEquals("CourseFileManager should return the correct modules metadata",
        "456", metadata.get("also a great name").getModuleId());
  }

  @Test
  public void testLoadWithNoCourseFile() throws IOException {
    boolean courseFileExists = manager.load(project);
    Assert.assertFalse("Load should return false when no course file exists", courseFileExists);
  }

  @Test
  public void testAddEntryForModule() throws IOException, InterruptedException {
    URL url = new URL("http://localhost:8000");
    CourseFileManager manager = CourseFileManager.getInstance();
    manager.createAndLoad(project, url);
    ModelFactory modelFactory = new ModelExtensions.TestModelFactory();
    for (int i = 0; i < numThreads; ++i) {
      Module module = modelFactory.createModule("name" + i, url, "id" + i);
      Runnable runnable = () -> {
        try {
          manager.addEntryForModule(module);
        } catch (IOException e) {
          threadFailed.set(true);
        }
      };
      threads.add(new Thread(runnable));
    }

    startAndJoinEach(threads);

    Assert.assertFalse(threadFailed.get());

    JSONObject jsonObject = getCourseFileContents();
    JSONObject modulesObject = jsonObject.getJSONObject("modules");
    Set<String> keys = modulesObject.keySet();
    Assert.assertEquals("The file should contain the correct number of module entries",
        numThreads, keys.size());
    for (String key : keys) {
      // This will throw and fail this test if the JSON format in the file is malformed
      modulesObject.getJSONObject(key).getString("id");
      modulesObject.getJSONObject(key).getString("downloadedAt");
    }
  }

  @Test
  public void testGetModulesMetadata() throws IOException, InterruptedException {
    JSONObject modulesObject = new JSONObject();
    for (int i = 0; i < numThreads; ++i) {
      String moduleName = "module" + i;
      String moduleId = "version" + i;
      ZonedDateTime downloadedAt = ZonedDateTime.now();

      modulesObject.put(moduleName,
          new JSONObject().put("id", moduleId).put("downloadedAt", downloadedAt)
      );

      threads.add(new Thread(() -> {
        IntelliJModuleMetadata metadata
            = CourseFileManager.getInstance().getModulesMetadata().get(moduleName);

        threadFailed.compareAndSet(false, !moduleId.equals(metadata.getModuleId()));
        threadFailed.compareAndSet(false, !downloadedAt.equals(metadata.getDownloadedAt()));
      }));
    }

    JSONObject courseFileJson = new JSONObject()
        .put("modules", modulesObject)
        .put("url", new URL("https://example.com"));

    FileUtils.writeStringToFile(courseFile, courseFileJson.toString(), StandardCharsets.UTF_8);

    CourseFileManager.getInstance().load(project);

    startAndJoinEach(threads);

    if (threadFailed.get()) {
      Assert.fail("CourseFileManager#getModulesMetadata did not return the correct map");
    }
  }
}
