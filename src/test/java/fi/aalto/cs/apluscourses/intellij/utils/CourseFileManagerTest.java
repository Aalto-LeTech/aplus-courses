package fi.aalto.cs.apluscourses.intellij.utils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleMetadata;
import fi.aalto.cs.apluscourses.utils.Version;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseFileManagerTest {

  // CourseFileManager is supposed to be thread safe, so all of these tests use multiple threads.
  private static final int NUM_THREADS = 16;

  private static final String URL_KEY = "url";
  private static final String LANGUAGE_KEY = "language";
  private static final String MODULES_KEY = "modules";
  private static final String MODULE_VERSION_KEY = "version";
  private static final String MODULE_DOWNLOADED_AT_KEY = "downloadedAt";

  private CourseFileManager manager;
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
  @BeforeEach
  void initializeObjects() throws IOException {
    Project project = mock(Project.class);
    File tempDir = FileUtilRt.createTempDirectory("test", "", true);
    doReturn(tempDir.toString()).when(project).getBasePath();
    FileUtilRt.createTempDirectory(tempDir, Project.DIRECTORY_STORE_FOLDER, "", true);
    courseFile = Paths
        .get(tempDir.toString(), Project.DIRECTORY_STORE_FOLDER, "a-plus-project.json")
        .toFile();
    threads = new ArrayList<>(NUM_THREADS);
    threadFailed = new AtomicBoolean(false);
    var notifier = mock(Notifier.class);

    ModuleManager moduleManager = mock(ModuleManager.class);
    when(project.getComponent(ModuleManager.class)).thenReturn(moduleManager);
    when(moduleManager.getModules()).thenReturn(new com.intellij.openapi.module.Module[0]);
    manager = new CourseFileManager(project, notifier);
  }

  @Test
  void testCreateAndLoad() throws IOException, InterruptedException {
    Assertions.assertFalse(courseFile.exists());

    URL url = new URL("http://localhost:3000");
    String language = "fi";

    for (int i = 0; i < NUM_THREADS; ++i) {
      threads.add(new Thread(() -> {
        try {
          manager.createAndLoad(url, language);
        } catch (IOException e) {
          threadFailed.set(true);
        }
      }));
    }

    startAndJoinEach(threads);

    Assertions.assertFalse(threadFailed.get());

    Assertions.assertEquals(url, manager.getCourseUrl(), "CourseFileManager returns the correct url");
    Assertions.assertEquals(language, manager.getLanguage(), "CourseFileManager returns the correct language");
    Assertions.assertTrue(manager.getModulesMetadata().isEmpty(),
        "CourseFileManager returns an empty map for modules metadata");

    JSONObject jsonObject = getCourseFileContents();
    URL jsonUrl = new URL(jsonObject.getString(URL_KEY));
    String jsonLanguage = jsonObject.getString(LANGUAGE_KEY);
    Assertions.assertEquals(url, jsonUrl, "The course file contains JSON with the given URL");
    Assertions.assertEquals(language, jsonLanguage, "The course file contains JSON with the given language");
  }

  @Test
  void testCreateAndLoadWithExistingFile() throws IOException {
    URL oldUrl = new URL("https://google.com");
    URL newUrl = new URL("https://github.com");
    String oldLanguage = "en";
    String newLanguage = "fi";

    JSONObject modulesObject = new JSONObject().put("awesome module",
        new JSONObject()
            .put(MODULE_VERSION_KEY, "5.4")
            .put(MODULE_DOWNLOADED_AT_KEY, ZonedDateTime.now())
    );
    JSONObject jsonObject = new JSONObject()
        .put(URL_KEY, oldUrl)
        .put(LANGUAGE_KEY, oldLanguage)
        .put(MODULES_KEY, modulesObject);

    FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);

    Assertions.assertTrue(courseFile.exists());

    manager.createAndLoad(newUrl, newLanguage);

    Assertions.assertEquals(newUrl, manager.getCourseUrl(),
        "CourseFileManager#createAndLoad overwrites the URL of the existing course file");
    Assertions.assertEquals(newLanguage, manager.getLanguage(),
        "CourseFileManager#createAndLoad overwrites the language of the existing course file");
    Assertions.assertEquals(1, manager.getModulesMetadata().size(),
        "CourseFileManager#createAndLoad gets the existing modules metadata");
    Assertions.assertEquals(new Version(5, 4), manager.getModulesMetadata().get("awesome module").getVersion(),
        "CourseFileManager#createAndLoad gets the existing modules metadata");
  }

  @Test
  void testLoad() throws IOException, InterruptedException {
    JSONObject jsonObject = new JSONObject()
        .put(URL_KEY, new URL("https://example.org"))
        .put(LANGUAGE_KEY, "de")
        .put(MODULES_KEY, new JSONObject()
            .put("great name", new JSONObject()
                .put(MODULE_VERSION_KEY, "1.2")
                .put(MODULE_DOWNLOADED_AT_KEY, ZonedDateTime.now()))
            .put("also a great name", new JSONObject()
                .put(MODULE_VERSION_KEY, "3.4")
                .put(MODULE_DOWNLOADED_AT_KEY, ZonedDateTime.now())));

    FileUtils.writeStringToFile(courseFile, jsonObject.toString(), StandardCharsets.UTF_8);

    for (int i = 0; i < NUM_THREADS; ++i) {
      threads.add(new Thread(() -> {
        try {
          // Load should return true
          threadFailed.compareAndSet(false, !manager.load());
        } catch (IOException e) {
          threadFailed.set(true);
        }
      }));
    }

    startAndJoinEach(threads);

    Assertions.assertEquals(new URL("https://example.org"), manager.getCourseUrl(),
        "CourseFileManager returns the correct URL");
    Assertions.assertEquals("de", manager.getLanguage(), "CourseFileManager returns the correct language");
    Map<String, ModuleMetadata> metadata = manager.getModulesMetadata();
    Assertions.assertEquals(2, metadata.size(),
        "CourseFileManager should return the correct number of modules metadata");
    Assertions.assertEquals(new Version(1, 2), metadata.get("great name").getVersion(),
        "CourseFileManager should return the correct modules metadata");
    Assertions.assertEquals(new Version(3, 4), metadata.get("also a great name").getVersion(),
        "CourseFileManager should return the correct modules metadata");
  }

  @Test
  void testLoadWithNoCourseFile() throws IOException {
    boolean courseFileExists = manager.load();
    Assertions.assertFalse(courseFileExists, "Load should return false when no course file exists");
  }

  @Test
  void testAddEntryForModule() throws IOException, InterruptedException {
    URL url = new URL("http://localhost:8000");
    manager.createAndLoad(url, "en");
    for (int i = 0; i < NUM_THREADS; ++i) {
      Module module =
          new ModelExtensions.TestModule("name" + i, url, new Version(i, 0), new Version(i, i),
              "changelog" + i, ZonedDateTime.now());
      Runnable runnable = () -> {
        try {
          manager.addModuleEntry(module);
        } catch (IOException e) {
          threadFailed.set(true);
        }
      };
      threads.add(new Thread(runnable));
    }

    startAndJoinEach(threads);

    Assertions.assertFalse(threadFailed.get());

    JSONObject jsonObject = getCourseFileContents();
    JSONObject modulesObject = jsonObject.getJSONObject(MODULES_KEY);
    Set<String> keys = modulesObject.keySet();
    Assertions.assertEquals(NUM_THREADS, keys.size(), "The file should contain the correct number of module entries");
    for (String key : keys) {
      // This will throw and fail this test if the JSON format in the file is malformed
      modulesObject.getJSONObject(key).getString(MODULE_VERSION_KEY);
      modulesObject.getJSONObject(key).getString(MODULE_DOWNLOADED_AT_KEY);
    }
  }

  @Test
  void testGetModulesMetadata() throws IOException, InterruptedException {
    Assertions.assertTrue(manager.getModulesMetadata().isEmpty(),
        "Course file manager returns empty metadata map when no course file has been loaded");

    JSONObject modulesObject = new JSONObject();
    for (int i = 0; i < NUM_THREADS; ++i) {
      String moduleName = "module" + i;
      Version moduleVersion = new Version(i, 0);
      ZonedDateTime downloadedAt = ZonedDateTime.now();

      modulesObject.put(moduleName, new JSONObject().put(
          MODULE_VERSION_KEY, moduleVersion.toString()).put(MODULE_DOWNLOADED_AT_KEY, downloadedAt)
      );

      threads.add(new Thread(() -> {
        ModuleMetadata metadata = manager.getModulesMetadata().get(moduleName);
        threadFailed.compareAndSet(false, !moduleVersion.equals(metadata.getVersion()));
        threadFailed.compareAndSet(false, !downloadedAt.equals(metadata.getDownloadedAt()));
      }));
    }

    JSONObject courseFileJson = new JSONObject()
        .put(MODULES_KEY, modulesObject)
        .put(URL_KEY, new URL("https://example.com"))
        .put(LANGUAGE_KEY, "fr");

    FileUtils.writeStringToFile(courseFile, courseFileJson.toString(), StandardCharsets.UTF_8);

    manager.load();

    startAndJoinEach(threads);

    if (threadFailed.get()) {
      Assertions.fail("CourseFileManager#getModulesMetadata did not return the correct map");
    }
  }

}
