package fi.aalto.cs.apluscourses.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ModelExtensions {

  public static class TestModule extends Module {

    private static URL testURL;

    static {
      try {
        testURL = new URL("https://example.com");
      } catch (MalformedURLException e) {
        e.printStackTrace();
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
      super(name, url);
    }

    @NotNull
    @Override
    public List<String> getDependencies() throws ModuleLoadException {
      return Collections.emptyList();
    }

    @Override
    public void fetch() throws IOException {
      // do nothing
    }

    @Override
    public void load() throws ModuleLoadException {
      // do nothing
    }
  }

  public static class TestModelFactory implements ModelFactory {

    @Override
    public Course createCourse(@NotNull String name,
                               @NotNull List<Module> modules,
                               @NotNull Map<String, String> requiredPlugins) {
      return new Course(name, modules, requiredPlugins);
    }

    @Override
    public Module createModule(@NotNull String name, @NotNull URL url) {
      return new TestModule(name, url);
    }
  }
}
