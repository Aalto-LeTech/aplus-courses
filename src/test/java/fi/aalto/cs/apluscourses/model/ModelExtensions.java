package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelExtensions {

  private static final Logger logger = LoggerFactory.getLogger(ModelExtensions.class);

  private ModelExtensions() {

  }

  public static class TestModule extends Module {

    private static URL testURL;

    static {
      try {
        testURL = new URL("https://example.com");
      } catch (MalformedURLException e) {
        logger.error("Test URL is malformed", e);
      }
    }

    public TestModule(@NotNull String name) {
      this(name, testURL);
    }

    /**
     * Constructs a test module with the given name and URL.
     *
     * @param name The name of the module.
     * @param url  The URL from which the module can be downloaded.
     */
    public TestModule(@NotNull String name, @NotNull URL url) {
      super(name, url, NOT_INSTALLED);
    }

    @NotNull
    @Override
    public List<String> getDependencyModules() throws ComponentLoadException {
      return Collections.emptyList();
    }

    @Override
    public List<String> getLibraries() throws ComponentLoadException {
      return Collections.emptyList();
    }

    @NotNull
    @Override
    public Path getPath() {
      return Paths.get(name);
    }

    @Override
    public void fetch() throws IOException {
      // do nothing
    }

    @Override
    public void load() throws ComponentLoadException {
      // do nothing
    }
  }

  public static class TestModelFactory implements ModelFactory {

    @Override
    public Course createCourse(@NotNull String name,
                               @NotNull List<Module> modules,
                               @NotNull List<Library> libraries,
                               @NotNull Map<String, String> requiredPlugins) {
      return new Course(name, modules, libraries, requiredPlugins);
    }

    @Override
    public Module createModule(@NotNull String name, @NotNull URL url) {
      return new TestModule(name, url);
    }

    @Override
    public Library createLibrary(@NotNull String name) {
      return null;
    }
  }
}
