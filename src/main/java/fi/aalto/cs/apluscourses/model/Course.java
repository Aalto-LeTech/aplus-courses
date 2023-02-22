package fi.aalto.cs.apluscourses.model;

import com.intellij.openapi.util.SystemInfoRt;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.JsonUtil;
import fi.aalto.cs.apluscourses.utils.PluginDependency;
import fi.aalto.cs.apluscourses.utils.ResourceException;
import fi.aalto.cs.apluscourses.utils.Resources;
import fi.aalto.cs.apluscourses.utils.Version;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public abstract class Course implements ComponentSource {

  @NotNull
  private final String id;

  @NotNull
  private final String name;

  @NotNull
  private final String aplusUrl;

  @NotNull
  private final List<String> languages;

  @NotNull
  private final List<Module> modules;

  @NotNull
  private final List<Library> libraries;

  @NotNull
  private final Map<Long, Map<String, String>> exerciseModules;

  @NotNull
  private final Map<String, URL> resourceUrls;

  @NotNull
  private final Map<String, String> vmOptions;

  @NotNull
  private final Set<String> optionalCategories;

  @NotNull
  private final List<String> autoInstallComponentNames;

  @NotNull
  protected final Map<String, Component> components;

  @NotNull
  private final Map<String, String[]> replInitialCommands;

  @NotNull
  private final String replAdditionalArguments;

  @NotNull
  private final Version courseVersion;

  @NotNull
  private final Map<Long, Tutorial> tutorials;

  @NotNull
  private final List<PluginDependency> pluginDependencies;

  @Nullable
  private final String feedbackParser;

  @Nullable
  private final String newsParser;

  /**
   * Constructs a course with the given parameters.
   *
   * @param name                The name of the course.
   * @param modules             The list of modules in the course.
   * @param resourceUrls        A map containing URLs to resources related to the course. The keys
   *                            are the names of the resources and the values are the URLs.
   * @param replInitialCommands A {@link Map}, with module name {@link String} as a key and a
   *                            {@link String} array of the commands to be executed on REPL
   */
  protected Course(@NotNull String id,
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
                   @Nullable String feedbackParser,
                   @Nullable String newsParser) {
    this.id = id;
    this.name = name;
    this.aplusUrl = aplusUrl;
    this.languages = languages;
    this.modules = modules;
    this.resourceUrls = resourceUrls;
    this.vmOptions = vmOptions;
    this.libraries = libraries;
    this.exerciseModules = exerciseModules;
    this.optionalCategories = optionalCategories;
    this.autoInstallComponentNames = autoInstallComponentNames;
    this.tutorials = tutorials;
    this.pluginDependencies = pluginDependencies;
    this.feedbackParser = feedbackParser;
    this.newsParser = newsParser;
    this.components = Stream.concat(modules.stream(), libraries.stream())
        .collect(Collectors.toMap(Component::getName, Function.identity()));
    this.replInitialCommands = replInitialCommands;
    this.replAdditionalArguments = replAdditionalArguments;
    this.courseVersion = courseVersion;
  }

  @NotNull
  public static Course fromResource(@NotNull String resourceName, @NotNull ModelFactory factory)
      throws ResourceException, MalformedCourseConfigurationException {
    Reader reader = new InputStreamReader(Resources.DEFAULT.getStream(resourceName));
    return fromConfigurationData(reader, resourceName, factory);
  }

  /**
   * Creates a course instance from the course configuration data in the given reader.
   *
   * @param reader A reader providing a character stream with the course configuration data.
   * @return A course instance containing the information parsed from the configuration data.
   * @throws MalformedCourseConfigurationException If the configuration data is malformed in any
   *                                               way
   */
  @NotNull
  public static Course fromConfigurationData(@NotNull Reader reader, @NotNull ModelFactory factory)
      throws MalformedCourseConfigurationException {
    return fromConfigurationData(reader, "", factory);
  }

  /**
   * Creates a course instance from the course configuration data in the given reader.
   *
   * @param reader     A reader providing a character stream with the course configuration data.
   * @param sourcePath The path to the source of the reader, which is stored in exceptions thrown
   *                   from this method.
   * @return A course instance containing the information parsed from the configuration data.
   * @throws MalformedCourseConfigurationException If the configuration data is malformed in any
   *                                               way
   */
  @NotNull
  public static Course fromConfigurationData(@NotNull Reader reader,
                                             @NotNull String sourcePath,
                                             @NotNull ModelFactory factory)
      throws MalformedCourseConfigurationException {
    JSONObject jsonObject = getCourseJsonObject(reader, sourcePath);
    String courseId = getCourseId(jsonObject, sourcePath);
    String courseName = getCourseName(jsonObject, sourcePath);
    String aplusUrl = getCourseAPlusUrl(jsonObject, sourcePath);
    List<String> languages = getCourseLanguages(jsonObject, sourcePath);
    List<Module> courseModules = getCourseModules(jsonObject, sourcePath, factory);
    Map<Long, Map<String, String>> exerciseModules
        = getCourseExerciseModules(jsonObject, sourcePath);
    Map<String, URL> resourceUrls = getCourseResourceUrls(jsonObject, sourcePath);
    Map<String, String> vmOptions = getInitVMOptions(jsonObject, sourcePath);
    Set<String> optionalCategories = getCourseOptionalCategories(jsonObject, sourcePath);
    List<String> autoInstallComponentNames
        = getCourseAutoInstallComponentNames(jsonObject, sourcePath);
    Map<String, String[]> replInitialCommands
        = getCourseReplInitialCommands(jsonObject, sourcePath);
    String replAdditionalArguments = getCourseReplAdditionalArguments(jsonObject, sourcePath);
    Version courseVersion = getCourseVersion(jsonObject, sourcePath);
    Map<Long, Tutorial> tutorials = getTutorials(jsonObject);
    List<PluginDependency> pluginDependencies = getPluginDependencies(jsonObject, sourcePath);
    String feedbackParser = jsonObject.optString("feedbackParser", null);
    String newsParser = jsonObject.optString("newsParser", null);
    long courseLastModified = jsonObject.optLong("courseLastModified");
    return factory.createCourse(
        courseId,
        courseName,
        aplusUrl,
        languages,
        courseModules,
        Collections.emptyList(), // libraries
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
        feedbackParser,
        newsParser,
        courseLastModified
    );
  }

  /**
   * Creates a course instance from the course configuration file at the given URL.
   *
   * @param url The URL of the course configuration file.
   * @return A course instance containing the information parsed from the course configuration file.
   * @throws IOException                           If an IO error occurs (network connection
   *                                               issues for an example).
   * @throws MalformedCourseConfigurationException If the course configuration file is malformed
   *                                               in any way.
   */
  @NotNull
  public static Course fromUrl(@NotNull URL url, @NotNull ModelFactory modelFactory)
      throws IOException, MalformedCourseConfigurationException {
    InputStream inputStream = CoursesClient.fetch(url);
    return Course.fromConfigurationData(
        new InputStreamReader(inputStream, StandardCharsets.UTF_8), url.toString(), modelFactory);
  }

  /**
   * Returns the id of the course.
   */
  @NotNull
  public String getId() {
    return id;
  }

  /**
   * Returns the name of the course.
   *
   * @return The name of the course.
   */
  @NotNull
  public String getName() {
    return name;
  }


  /**
   * Returns the languages of the course (as ISO 639-1 codes).
   *
   * @return A list of language codes.
   */
  @NotNull
  public List<String> getLanguages() {
    return languages;
  }

  /**
   * Returns the list of all modules in this course. If the course object is created with {@link
   * Course#fromConfigurationData}, then the modules are returned in the order in which they are
   * listed in the course configuration data.
   *
   * @return All modules of this course.
   */
  @NotNull
  public List<Module> getModules() {
    return Collections.unmodifiableList(modules);
  }

  /**
   * Returns the list of libraries (not including common libraries) of the course.
   *
   * @return Libraries of this course.
   */
  @NotNull
  public List<Library> getLibraries() {
    return Collections.unmodifiableList(libraries);
  }

  @NotNull
  public Collection<Component> getComponents() {
    return components.values();
  }

  /**
   * Returns a mapping of exercise IDs to modules. The keys are exercise IDs, and the values are
   * maps from language codes to module names. Note, that some exercises use modules that are not in
   * the course configuration file, so the modules may not be in {@link Course#getModules}.
   */
  @NotNull
  public Map<Long, Map<String, String>> getExerciseModules() {
    return Collections.unmodifiableMap(exerciseModules);
  }

  /**
   * Returns a map containing URLs of resources for the course. The keys are the names of the
   * resources and the values are the URLs.
   *
   * @return A map with URLs for various resources related to the course.
   */
  @NotNull
  public Map<String, URL> getResourceUrls() {
    return Collections.unmodifiableMap(resourceUrls);
  }

  @NotNull
  public Version getVersion() {
    return courseVersion;
  }

  /**
   * Returns a list of components that should be installed automatically for this course.
   *
   * @return A list of components that should be installed automatically for this course.
   */
  @NotNull
  public List<Component> getAutoInstallComponents() {
    return autoInstallComponentNames
        .stream()
        .map(this::getComponentIfExists)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Returns a URL containing the appropriate IDE settings for the platform that the user
   * is currently using. If no IDE settings are available, null is returned.
   */
  @Nullable
  public URL getAppropriateIdeSettingsUrl() {
    URL ideSettingsUrl = null;

    if (SystemInfoRt.isWindows) {
      ideSettingsUrl = resourceUrls.get("ideSettingsWindows");
    } else if (SystemInfoRt.isLinux) {
      ideSettingsUrl = resourceUrls.get("ideSettingsLinux");
    } else if (SystemInfoRt.isMac) {
      ideSettingsUrl = resourceUrls.get("ideSettingsMac");
    }

    if (ideSettingsUrl == null) {
      ideSettingsUrl = resourceUrls.get("ideSettings");
    }

    return ideSettingsUrl;
  }

  @NotNull
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public Map<String, String> getVMOptions() {
    return vmOptions;
  }

  @NotNull
  public Set<String> getOptionalCategories() {
    return optionalCategories;
  }

  @NotNull
  private static JSONObject getCourseJsonObject(@NotNull Reader reader, @NotNull String source)
      throws MalformedCourseConfigurationException {
    JSONTokener tokenizer = new JSONTokener(reader);
    try {
      return new JSONObject(tokenizer);
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationException(source,
          "Course configuration file should consist of a valid JSON object", ex);
    }
  }

  @NotNull
  private static String getCourseId(@NotNull JSONObject jsonObject, @NotNull String source)
      throws MalformedCourseConfigurationException {
    try {
      return jsonObject.getString("id");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationException(source,
          "Missing or malformed \"id\" key", ex);
    }
  }

  @NotNull
  private static String getCourseName(@NotNull JSONObject jsonObject, @NotNull String source)
      throws MalformedCourseConfigurationException {
    try {
      return jsonObject.getString("name");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationException(source,
          "Missing or malformed \"name\" key", ex);
    }
  }

  @NotNull
  private static String getCourseAPlusUrl(@NotNull JSONObject jsonObject, @NotNull String source)
      throws MalformedCourseConfigurationException {
    try {
      return jsonObject.getString("aPlusUrl");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationException(source,
          "Missing or malformed \"aPlusUrl\" key", ex);
    }
  }

  @NotNull
  private static List<String> getCourseLanguages(@NotNull JSONObject jsonObject,
                                                 @NotNull String source)
      throws MalformedCourseConfigurationException {
    JSONArray languagesJson = jsonObject.optJSONArray("languages");
    if (languagesJson == null) {
      throw new MalformedCourseConfigurationException(
          source, "Missing or malformed \"languages\" key", null);
    }
    List<String> languages = new ArrayList<>();
    for (int i = 0; i < languagesJson.length(); ++i) {
      try {
        languages.add(languagesJson.getString(i));
      } catch (JSONException e) {
        throw new MalformedCourseConfigurationException(
            source, "\"languages\" array should contain strings", e);
      }
    }
    return languages;
  }

  @NotNull
  private static List<Module> getCourseModules(@NotNull JSONObject jsonObject,
                                               @NotNull String source,
                                               @NotNull ModelFactory factory)
      throws MalformedCourseConfigurationException {
    JSONArray modulesJsonArray;
    try {
      modulesJsonArray = jsonObject.getJSONArray("modules");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationException(source,
          "Missing or malformed \"modules\" key", ex);
    }

    List<Module> modules = new ArrayList<>();
    // Indexing loop used to simplify checking that each entry is a JSON object.
    for (int i = 0; i < modulesJsonArray.length(); ++i) {
      try {
        JSONObject moduleObject = modulesJsonArray.getJSONObject(i);
        modules.add(Module.fromJsonObject(moduleObject, factory));
      } catch (JSONException ex) {
        throw new MalformedCourseConfigurationException(source,
            "\"modules\" value should be an array of objects containing module information", ex);
      } catch (MalformedURLException ex) {
        throw new MalformedCourseConfigurationException(source,
            "Malformed URL in module object", ex);
      }
    }
    return modules;
  }

  @NotNull
  private static Map<Long, Map<String, String>> getCourseExerciseModules(@NotNull JSONObject object,
                                                                         @NotNull String source)
      throws MalformedCourseConfigurationException {
    Map<Long, Map<String, String>> exerciseModules = new HashMap<>();
    JSONObject exerciseModulesJson = object.optJSONObject("exerciseModules");
    if (exerciseModulesJson == null) {
      return exerciseModules;
    }

    try {
      Iterable<String> keys = exerciseModulesJson::keys;
      for (String exerciseId : keys) {
        JSONObject modules = exerciseModulesJson.getJSONObject(exerciseId);
        Map<String, String> languageToModule = new HashMap<>();
        Iterable<String> languages = modules::keys;
        for (String language : languages) {
          languageToModule.put(language, modules.getString(language));
        }
        exerciseModules.put(Long.valueOf(exerciseId), languageToModule);
      }
      return exerciseModules;
    } catch (JSONException e) {
      throw new MalformedCourseConfigurationException(source,
          "Malformed \"exerciseModules\" object", e);
    }
  }

  @NotNull
  private static Map<String, URL> getCourseResourceUrls(@NotNull JSONObject jsonObject,
                                                        @NotNull String source)
      throws MalformedCourseConfigurationException {
    Map<String, URL> resourceUrls = new HashMap<>();
    JSONObject resourceUrlsJsonObject = jsonObject.optJSONObject("resources");
    if (resourceUrlsJsonObject == null) {
      return resourceUrls;
    }
    Iterable<String> keys = resourceUrlsJsonObject::keys;
    for (String resourceName : keys) {
      try {
        URL resourceUrl = new URL(resourceUrlsJsonObject.getString(resourceName));
        resourceUrls.put(resourceName, resourceUrl);
      } catch (JSONException | MalformedURLException ex) {
        throw new MalformedCourseConfigurationException(source,
            "Expected name-url-pairs in \"resources\" object", ex);
      }
    }
    return resourceUrls;
  }

  @NotNull
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private static Map<String, String> getInitVMOptions(@NotNull JSONObject jsonObject, @NotNull String source)
      throws MalformedCourseConfigurationException {
    Map<String, String> vmOptions = new HashMap<>();
    JSONObject vmOptionsJsonObject = jsonObject.optJSONObject("vmOptions");
    if (vmOptionsJsonObject == null) {
      return vmOptions;
    }

    Iterable<String> keys = vmOptionsJsonObject::keys;
    for (var optionKey : keys) {
      try {
        vmOptions.put(optionKey, vmOptionsJsonObject.getString(optionKey));
      } catch (JSONException ex) {
        throw new MalformedCourseConfigurationException(source,
            "Expected string-typed key-value pairs in \"vmOptions\" object", ex);
      }
    }

    return vmOptions;
  }

  @NotNull
  private static Set<String> getCourseOptionalCategories(@NotNull JSONObject jsonObject,
                                                         @NotNull String source)
      throws MalformedCourseConfigurationException {
    JSONArray categoriesJson = jsonObject.optJSONArray("optionalCategories");
    if (categoriesJson == null) {
      return Set.of("training", "challenge"); // defaults for older O1 courses
    }
    Set<String> categories = new HashSet<>();
    for (int i = 0; i < categoriesJson.length(); ++i) {
      try {
        categories.add(categoriesJson.getString(i));
      } catch (JSONException e) {
        throw new MalformedCourseConfigurationException(
            source, "\"optionalCategories\" array should contain strings", e);
      }
    }
    return categories;
  }

  @NotNull
  private static List<String> getCourseAutoInstallComponentNames(@NotNull JSONObject jsonObject,
                                                                 @NotNull String source)
      throws MalformedCourseConfigurationException {
    List<String> autoInstallComponentNames = new ArrayList<>();
    JSONArray autoInstallArray = jsonObject.optJSONArray("autoInstall");
    if (autoInstallArray == null) {
      return autoInstallComponentNames;
    }

    for (int i = 0; i < autoInstallArray.length(); ++i) {
      try {
        String autoInstallComponentName = autoInstallArray.getString(i);
        autoInstallComponentNames.add(autoInstallComponentName);
      } catch (JSONException e) {
        throw new MalformedCourseConfigurationException(
            source, "Names in \"autoInstall\" array should be course components", e);
      }
    }
    return autoInstallComponentNames;
  }

  @NotNull
  private static Map<String, String[]> getCourseReplInitialCommands(@NotNull JSONObject jsonObject,
                                                                    @NotNull String source)
      throws MalformedCourseConfigurationException {
    Map<String, String[]> replInitialCommands = new HashMap<>();
    JSONObject replJson = jsonObject.optJSONObject("repl");
    if (replJson == null) {
      return replInitialCommands;
    }
    try {
      JSONObject initialCommandsJsonObject = jsonObject
          .getJSONObject("repl").getJSONObject("initialCommands");

      Iterable<String> keys = initialCommandsJsonObject::keys;
      for (String moduleName : keys) {
        String[] replCommands = initialCommandsJsonObject
            .getJSONArray(moduleName)
            .toList()
            .stream()
            .map(String.class::cast)
            .toArray(String[]::new);

        replInitialCommands.put(moduleName, replCommands);
      }
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationException(source,
          "Expected moduleName-commands-pairs in \"repl\" object", ex);
    }

    return replInitialCommands;
  }

  @NotNull
  private static String getCourseReplAdditionalArguments(@NotNull JSONObject jsonObject,
                                                         @NotNull String source)
      throws MalformedCourseConfigurationException {
    try {
      return jsonObject.optString("replArguments", "");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationException(source,
              "Malformed or non-string \"replArguments\" key", ex);
    }
  }

  @NotNull
  private static Version getCourseVersion(@NotNull JSONObject jsonObject,
                                          @NotNull String source)
      throws MalformedCourseConfigurationException {
    String versionJson = jsonObject.optString("version", null);
    if (versionJson == null) {
      return BuildInfo.INSTANCE.courseVersion;
    }

    try {
      return Version.fromString(versionJson);
    } catch (Version.InvalidVersionStringException ex) {
      throw new MalformedCourseConfigurationException(source,
          "Incomplete or invalid \"version\" object", ex);
    }
  }

  private static Map<Long, Tutorial> getTutorials(@NotNull JSONObject jsonObject) {
    JSONObject tutorialsJson = jsonObject.optJSONObject("tutorials");
    return tutorialsJson == null ? Collections.emptyMap()
        : JsonUtil.parseObject(tutorialsJson, JSONObject::getJSONObject,
        Tutorial::fromJsonObject, Long::valueOf);
  }

  @NotNull
  private static List<PluginDependency> getPluginDependencies(@NotNull JSONObject jsonObject,
                                                              @NotNull String source)
      throws MalformedCourseConfigurationException {
    JSONArray pluginsJson = jsonObject.optJSONArray("requiredPlugins");
    if (pluginsJson == null) {
      return Collections.emptyList();
    }

    List<PluginDependency> result = new ArrayList<>();

    try {
      for (int i = 0; i < pluginsJson.length(); ++i) {
        JSONObject currentPluginJson = pluginsJson.getJSONObject(i);
        result.add(new PluginDependency(currentPluginJson.getString("name"), currentPluginJson.getString("id")));
      }
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationException(source,
          "Malformed \"requiredPlugins\" array", ex);
    }

    return result;
  }

  public Map<Long, Tutorial> getTutorials() {
    return Collections.unmodifiableMap(tutorials);
  }

  @Nullable
  @Override
  public Component getComponentIfExists(@NotNull String name) {
    return components.get(name);
  }

  /**
   * Resolves states of unresolved components and calls {@code validate()}.
   */
  public void resolve() {
    getComponents().forEach(Component::resolveState);
    validate();
  }

  /**
   * Validates that components conform integrity constraints.
   */
  public void validate() {
    for (Component component : getComponents()) {
      component.validate(this);
    }
  }

  /**
   * Gets modules that could be updated.
   *
   * @return A list of modules.
   */
  @NotNull
  public List<Module> getUpdatableModules() {
    return getModules()
        .stream()
        .filter(Module::isUpdatable)
        .collect(Collectors.toList());
  }

  public void register() {
    // Subclasses may do things.
  }

  public void unregister() {
    // Subclasses may do things.
  }

  @NotNull
  public String getHtmlUrl() {
    return aplusUrl;
  }

  @NotNull
  public String getApiUrl() {
    return aplusUrl + "api/v2/";
  }

  @NotNull
  public Map<String, String[]> getReplInitialCommands() {
    return replInitialCommands;
  }

  @NotNull
  public String getReplAdditionalArguments() {
    return replAdditionalArguments;
  }

  @NotNull
  public List<String> getAutoInstallComponentNames() {
    return Collections.unmodifiableList(autoInstallComponentNames);
  }

  @NotNull
  public List<PluginDependency> getRequiredPlugins() {
    return Collections.unmodifiableList(pluginDependencies);
  }

  @NotNull
  public abstract ExerciseDataSource getExerciseDataSource();

  @Nullable
  public String getFeedbackParser() {
    return feedbackParser;
  }

  @Nullable
  public String getNewsParser() {
    return newsParser;
  }
}
