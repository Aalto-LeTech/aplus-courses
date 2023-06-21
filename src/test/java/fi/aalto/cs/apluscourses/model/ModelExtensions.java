package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.model.APlusProject;
import fi.aalto.cs.apluscourses.intellij.model.CommonLibraryProvider;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJCourse;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.Callbacks;
import fi.aalto.cs.apluscourses.utils.CourseHiddenElements;
import fi.aalto.cs.apluscourses.utils.PluginDependency;
import fi.aalto.cs.apluscourses.utils.Version;
import fi.aalto.cs.apluscourses.utils.cache.CachePreference;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ModelExtensions {

  private static final Logger logger = APlusLogger.logger;

  private ModelExtensions() {

  }

  public static class TestExerciseDataSource implements ExerciseDataSource {

    @NotNull
    @Override
    public List<Group> getGroups(@NotNull Course course, @NotNull Authentication authentication) {
      return Collections.singletonList(new Group(0,
          Collections.singletonList(new Group.GroupMember(1, "Only you"))));
    }

    @NotNull
    @Override
    public List<ExerciseGroup> getExerciseGroups(@NotNull Course course,
                                                 @NotNull Authentication authentication,
                                                 @NotNull String languageCode) {
      return Collections.emptyList();
    }

    @NotNull
    @Override
    public Points getPoints(@NotNull Course course, @NotNull Authentication authentication) {
      return new Points(
          Collections.emptyMap(),
          Collections.emptyMap(),
          Collections.emptyMap()
      );
    }

    @Override
    public @NotNull Points getPoints(@NotNull Course course,
                                     @NotNull Authentication authentication,
                                     @Nullable Student student) {
      return new Points(
          Collections.emptyMap(),
          Collections.emptyMap(),
          Collections.emptyMap());
    }

    @NotNull
    @Override
    public Exercise getExercise(long exerciseId,
                                @NotNull Points points,
                                @NotNull Set<String> optionalCategories,
                                @NotNull Map<Long, Tutorial> tutorials,
                                @NotNull Authentication authentication,
                                @NotNull CachePreference cachePreference,
                                @NotNull String languageCode) {
      return new Exercise(1, "lol", "http://example.com",
          new SubmissionInfo(Collections.emptyMap()), 20, 10, OptionalLong.empty(), null, false
      );
    }

    @Override
    public @NotNull String getSubmissionFeedback(long submissionId, @NotNull Authentication authentication)
        throws IOException {
      return "";
    }

    @NotNull
    @Override
    public SubmissionResult getSubmissionResult(@NotNull String submissionUrl,
                                                @NotNull Exercise exercise,
                                                @NotNull Authentication authentication,
                                                @NotNull Course course,
                                                @NotNull CachePreference cachePreference) {
      return new SubmissionResult(0, 20, 0.0, SubmissionResult.Status.GRADED, exercise);
    }

    @Override
    public @NotNull User getUser(@NotNull Authentication authentication) {
      return new User(authentication, "test", "123456", 3333);
    }

    @Override
    @NotNull
    public List<Student> getStudents(@NotNull Course course,
                                     @NotNull Authentication authentication,
                                     @NotNull CachePreference cachePreference) {
      return new ArrayList<>();
    }

    @Override
    public @NotNull ZonedDateTime getEndingTime(@NotNull Course course,
                                                @NotNull Authentication authentication) {
      return ZonedDateTime.of(2020, 1, 2, 0, 0, 0, 0,
          ZoneId.systemDefault());
    }

    @Override
    public @NotNull List<News> getNews(@NotNull Course course, @NotNull Authentication authentication,
                                       @NotNull String language) {
      return Collections.emptyList();
    }

    @Override
    public String submit(@NotNull Submission submission, @NotNull Authentication authentication) {
      // do nothing
      return "";
    }
  }

  public static class TestCourse extends Course {

    private final ExerciseDataSource exerciseDataSource;

    /**
     * Constructor matching superclass.
     */
    public TestCourse(@NotNull String id,
                      @NotNull String name,
                      @NotNull String aplusUrl,
                      @NotNull List<String> languages,
                      @NotNull List<Module> modules,
                      @NotNull List<Library> libraries,
                      @NotNull Map<Long, Map<String, String>> exerciseModules,
                      @NotNull Map<String, URL> resourceUrls,
                      @NotNull Map<String, String> vmOptions,
                      @NotNull Set<String> optionalCategories,
                      @NotNull List<String> autoInstallComponentNames,
                      @NotNull Map<String, String[]> replInitialCommands,
                      @NotNull String replAdditionalArguments,
                      @NotNull Version courseVersion,
                      @NotNull Map<Long, Tutorial> tutorials,
                      @NotNull List<PluginDependency> pluginDependencies,
                      @NotNull CourseHiddenElements hiddenElements,
                      @NotNull Callbacks callbacks) {
      super(id, name, aplusUrl, languages, modules, libraries, exerciseModules, resourceUrls, vmOptions,
          optionalCategories, autoInstallComponentNames, replInitialCommands, replAdditionalArguments,
          courseVersion, tutorials, pluginDependencies, hiddenElements, callbacks, null, "default");
      exerciseDataSource = new TestExerciseDataSource();
    }

    public TestCourse(@NotNull String id) {
      this(id, "", new TestExerciseDataSource());
    }

    /**
     * Creates a dummy {@link Course} for testing purposes.
     *
     * @param id                 {@link String} id for the {@link Course}
     * @param name               {@link String} for the {@link Course}.
     * @param exerciseDataSource Data source for exercises.
     */
    public TestCourse(@NotNull String id, @NotNull String name,
                      @NotNull ExerciseDataSource exerciseDataSource) {
      super(
          id,
          name,
          "https://example.com/",
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
          // optionalCategories
          Collections.emptySet(),
          // autoInstallComponentNames
          Collections.emptyList(),
          // replInitialCommands
          Collections.emptyMap(),
          // replAdditionalArguments
          "",
          // courseVersion
          BuildInfo.INSTANCE.courseVersion,
          // tutorials
          Collections.emptyMap(),
          // pluginDependencies
          Collections.emptyList(),
          // hiddenElements
          new CourseHiddenElements(),
          // callbacks
          new Callbacks(),
          null, "default");
      this.exerciseDataSource = exerciseDataSource;
    }

    @NotNull
    @Override
    public ExerciseDataSource getExerciseDataSource() {
      return exerciseDataSource;
    }
  }

  public static class TestIntelliJCourse extends IntelliJCourse {

    public TestIntelliJCourse(@NotNull String id,
                              @NotNull String name,
                              @NotNull APlusProject project,
                              @NotNull CommonLibraryProvider commonLibraryProvider) {
      this(id, name, Collections.emptyList(), project, commonLibraryProvider);
    }

    /**
     * Constructor for testing purposes that assumes reasonable defaults for most arguments.
     */
    public TestIntelliJCourse(@NotNull String id,
                              @NotNull String name,
                              @NotNull List<Module> modules,
                              @NotNull APlusProject project,
                              @NotNull CommonLibraryProvider commonLibraryProvider) {
      super(
          id,
          name,
          "",
          // languages
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
          // optionalCategories
          Collections.emptySet(),
          // autoInstallComponentNames
          Collections.emptyList(),
          // replInitialCommands
          Collections.emptyMap(),
          // replAdditionalArguments
          "",
          // courseVersion
          BuildInfo.INSTANCE.courseVersion,
          project,
          commonLibraryProvider,
          // tutorials
          Collections.emptyMap(),
          // pluginDependencies
          Collections.emptyList(),
          // hiddenElements
          new CourseHiddenElements(),
          // callbacks
          new Callbacks(),
          null,
          null,
          0);
    }
  }

  public static class TestComponent extends Component {

    public TestComponent() {
      super("");
    }

    public TestComponent(@NotNull String name) {
      super(name);
    }

    @NotNull
    @Override
    public Path getPath() {
      return Paths.get(getName());
    }

    @Override
    public void fetch() {
      // do nothing
    }

    @Override
    public void load() {
      // do nothing
    }

    @NotNull
    @Override
    public Path getFullPath() {
      return getPath().toAbsolutePath();
    }

    @Override
    protected int resolveStateInternal() {
      return 0;
    }

    @NotNull
    @Override
    protected List<String> computeDependencies() {
      return Collections.emptyList();
    }

    @Override
    public boolean isUpdatable() {
      return false;
    }

    @Override
    public boolean hasLocalChanges() {
      return false;
    }
  }

  public static class TestModule extends Module {

    private static URL testURL;

    static {
      try {
        testURL = new URL("https://example.com");
      } catch (MalformedURLException e) {
        logger.warn("Test URL is malformed", e);
      }
    }

    public TestModule(@NotNull String name) {
      this(name, testURL, new Version(1, 0), null, "changes", null);
    }

    public TestModule(@NotNull String name,
                      @NotNull URL url,
                      @NotNull Version version,
                      @Nullable Version localVersion,
                      @NotNull String changelog,
                      @Nullable ZonedDateTime downloadedAt) {
      super(name, url, changelog, version, localVersion, downloadedAt);
    }

    @NotNull
    @Override
    public Path getPath() {
      return Paths.get(name);
    }

    @Override
    public void fetchInternal() {
      // do nothing
    }

    @Override
    public void load() throws ComponentLoadException {
      // do nothing
    }

    @Override
    protected boolean hasLocalChanges(@NotNull ZonedDateTime downloadedAt) {
      return false;
    }

    @Override
    public Module copy(@NotNull String newName) {
      return new TestModule(newName, url, version, localVersion, changelog, downloadedAt);
    }

    @NotNull
    @Override
    public Path getFullPath() {
      return getPath().toAbsolutePath();
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

  public static class TestLibrary extends Library {

    public TestLibrary(@NotNull String name) {
      super(name);
    }

    @NotNull
    @Override
    public Path getPath() {
      return Paths.get("lib", name);
    }

    @Override
    public void fetch() {
      // do nothing
    }

    @Override
    public void load() {
      // do nothing
    }

    @NotNull
    @Override
    public Path getFullPath() {
      return getPath().toAbsolutePath();
    }

    @Override
    protected int resolveStateInternal() {
      return Component.NOT_INSTALLED;
    }
  }

  public static class TestModelFactory implements ModelFactory {

    @Override
    public Course createCourse(@NotNull String id,
                               @NotNull String name,
                               @NotNull String aplusUrl,
                               @NotNull List<String> languages,
                               @NotNull List<Module> modules,
                               @NotNull List<Library> libraries,
                               @NotNull Map<Long, Map<String, String>> exerciseModules,
                               @NotNull Map<String, URL> resourceUrls,
                               @NotNull Map<String, String> vmOptions,
                               @NotNull Set<String> optionalCategories,
                               @NotNull List<String> autoInstallComponentNames,
                               @NotNull Map<String, String[]> replInitialCommands,
                               @NotNull String replAdditionalArguments,
                               @NotNull Version courseVersion,
                               @NotNull Map<Long, Tutorial> tutorials,
                               @NotNull List<PluginDependency> pluginDependencies,
                               @NotNull CourseHiddenElements hiddenElements,
                               @NotNull Callbacks callbacks,
                               @Nullable String feedbackParser,
                               @Nullable String newsParser,
                               long courseLastModified) {
      return new ModelExtensions.TestCourse(
          id,
          name,
          aplusUrl,
          languages,
          modules,
          libraries,
          exerciseModules,
          resourceUrls,
          vmOptions,
          optionalCategories,
          autoInstallComponentNames,
          replInitialCommands,
          replAdditionalArguments,
          courseVersion,
          tutorials,
          pluginDependencies,
          hiddenElements,
          callbacks
      );
    }

    @Override
    public Module createModule(@NotNull String name,
                               @NotNull URL url,
                               @NotNull Version version,
                               @NotNull String changelog) {
      return new TestModule(name, url, version, null, changelog, null);
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
