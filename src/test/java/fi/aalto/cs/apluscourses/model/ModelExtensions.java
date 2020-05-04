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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelExtensions {

  private static final Logger logger = LoggerFactory.getLogger(ModelExtensions.class);

  private ModelExtensions() {

  }

  public static class TestComponent extends Component {

    public TestComponent(@NotNull String name) {
      super(name);
    }

    @NotNull
    @Override
    public Path getPath() {
      return null;
    }

    @Override
    public void fetch() throws IOException {

    }

    @Override
    public void load() throws ComponentLoadException {

    }

    @Override
    protected int resolveStateInternal() {
      return 0;
    }

    @NotNull
    @Override
    protected List<String> computeDependencies() {
      return null;
    }
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

    public TestModule(@NotNull String name, @NotNull URL url) {
      super(name, url);
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

    @Override
    protected int resolveStateInternal() {
      return Component.NOT_INSTALLED;
    }

    @NotNull
    @Override
    protected List<String> computeDependencies() {
      return Collections.emptyList();
    }
  }

  public static class TestModelFactory implements ModelFactory {

    @Override
    public Course createCourse(@NotNull String name,
                               @NotNull List<Module> modules,
                               @NotNull List<Library> libraries,
                               @NotNull Map<String, String> requiredPlugins,
                               @NotNull Map<String, URL> resourceUrls) {
      return new Course(name, modules, libraries, requiredPlugins, resourceUrls,
          new TestComponentSource());
    }

    @Override
    public Module createModule(@NotNull String name, @NotNull URL url) {
      return new TestModule(name, url);
    }

    @Override
    public Library createLibrary(@NotNull String name) {
      throw new UnsupportedOperationException("Only common libraries are supported.");
    }
  }

  public static class TestComponentSource implements ComponentSource {

    @Nullable
    @Override
    public Component getComponentIfExists(@NotNull String componentName) {
      return null;
    }
  }
}
