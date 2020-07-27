package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Course implements ComponentSource {
  @NotNull
  private final String id;

  @NotNull
  private final String name;

  @NotNull
  private final List<Module> modules;

  @NotNull
  private Points submissionResultsList;

  @NotNull
  private final List<Library> libraries;

  @NotNull
  private final Map<Long, Map<String, String>> exerciseModules;

  // Maps ids of required plugins to the names of required plugins.
  @NotNull
  private final Map<String, String> requiredPlugins;

  @NotNull
  private final Map<String, URL> resourceUrls;

  @NotNull
  private final List<String> autoInstallComponentNames;

  @NotNull
  protected final Map<String, Component> components;

  //todo: javadoc is out of date
  /**
   * Constructs a course with the given parameters.
   *
   * @param name            The name of the course
   * @param modules         The list of modules in the course.
   * @param requiredPlugins A map containing the required plugins for this course. The keys are the
   *                        ids of the plugins and the values are the names of the plugins.
   * @param resourceUrls    A map containing URLs to resources related to the course. The keys are
   *                        the names of the resources and the values are the URLs.
   */
  public Course(@NotNull String id,
                @NotNull String name,
                @NotNull List<Module> modules,
                @NotNull List<Library> libraries,
                @NotNull Map<Long, Map<String, String>> exerciseModules,
                @NotNull Map<String, String> requiredPlugins,
                @NotNull Map<String, URL> resourceUrls,
                @NotNull List<String> autoInstallComponentNames) {
    this.id = id;
    this.name = name;
    this.modules = modules;
    this.libraries = libraries;
    this.exerciseModules = exerciseModules;
    this.requiredPlugins = requiredPlugins;
    this.resourceUrls = resourceUrls;
    this.autoInstallComponentNames = autoInstallComponentNames;
    this.components = Stream.concat(modules.stream(), libraries.stream())
        .collect(Collectors.toMap(Component::getName, Function.identity()));
  }

  /**
   * Creates a course instance from the course configuration data in the given reader.
   *
   * @param reader A reader providing a character stream with the course configuration data.
   * @return A course instance containing the information parsed from the configuration data.
   * @throws MalformedCourseConfigurationFileException If the configuration data is malformed in
   *                                                   any way
   */
  @NotNull
  public static Course fromConfigurationData(@NotNull Reader reader, @NotNull ModelFactory factory)
      throws MalformedCourseConfigurationFileException {
    return fromConfigurationData(reader, "", factory);
  }

  /**
   * Creates a course instance from the course configuration data in the given reader.
   *
   * @param reader     A reader providing a character stream with the course configuration data.
   * @param sourcePath The path to the source of the reader, which is stored in exceptions thrown
   *                   from this method.
   * @return A course instance containing the information parsed from the configuration data.
   * @throws MalformedCourseConfigurationFileException If the configuration data is malformed in
   *                                                   any way
   */
  @NotNull
  public static Course fromConfigurationData(@NotNull Reader reader,
                                             @NotNull String sourcePath,
                                             @NotNull ModelFactory factory)
      throws MalformedCourseConfigurationFileException {
    JSONObject jsonObject = getCourseJsonObject(reader, sourcePath);
    String courseId = getCourseId(jsonObject, sourcePath);
    String courseName = getCourseName(jsonObject, sourcePath);
    List<Module> courseModules = getCourseModules(jsonObject, sourcePath, factory);
    Map<Long, Map<String, String>> exerciseModules
        = getCourseExerciseModules(jsonObject, sourcePath);
    Map<String, String> requiredPlugins = getCourseRequiredPlugins(jsonObject, sourcePath);
    Map<String, URL> resourceUrls = getCourseResourceUrls(jsonObject, sourcePath);
    List<String> autoInstallComponentNames
        = getCourseAutoInstallComponentNames(jsonObject, sourcePath);
    return factory.createCourse(courseId, courseName, courseModules, Collections.emptyList(),
        exerciseModules, requiredPlugins, resourceUrls, autoInstallComponentNames);
  }

  /**
   * Creates a course instance from the course configuration file at the given URL.
   *
   * @param url The URL of the course configuration file.
   * @return A course instance containing the information parsed from the course configuration file.
   * @throws IOException                               If an IO error occurs (network connection
   *                                                   issues for an example).
   * @throws MalformedCourseConfigurationFileException If the course configuration file is malformed
   *                                                   in any way.
   */
  @NotNull
  public static Course fromUrl(@NotNull URL url, @NotNull ModelFactory modelFactory)
      throws IOException, MalformedCourseConfigurationFileException {
    InputStream inputStream = CoursesClient.fetch(url);
    return Course.fromConfigurationData(
        new InputStreamReader(inputStream), url.toString(), modelFactory);
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
   * Returns the list of all modules in this course. If the course object is created with
   * {@link Course#fromConfigurationData}, then the modules are returned in the order in which they
   * are listed in the course configuration data.
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
   * Returns a map containing the required plugins for the course. The keys are the ids of the
   * plugins and the values are the names corresponding to the ids.
   *
   * @return A map with the required plugins of the course.
   */
  @NotNull
  public Map<String, String> getRequiredPlugins() {
    return Collections.unmodifiableMap(requiredPlugins);
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


  @NotNull
  private static JSONObject getCourseJsonObject(@NotNull Reader reader, @NotNull String source)
      throws MalformedCourseConfigurationFileException {
    JSONTokener tokenizer = new JSONTokener(reader);
    try {
      return new JSONObject(tokenizer);
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(source,
          "Course configuration file should consist of a valid JSON object", ex);
    }
  }

  @NotNull
  private static String getCourseId(@NotNull JSONObject jsonObject, @NotNull String source)
      throws MalformedCourseConfigurationFileException {
    try {
      return jsonObject.getString("id");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(source,
          "Missing or malformed \"id\" key", ex);
    }
  }

  @NotNull
  private static String getCourseName(@NotNull JSONObject jsonObject, @NotNull String source)
      throws MalformedCourseConfigurationFileException {
    try {
      return jsonObject.getString("name");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(source,
          "Missing or malformed \"name\" key", ex);
    }
  }

  @NotNull
  private static List<Module> getCourseModules(@NotNull JSONObject jsonObject,
                                               @NotNull String source,
                                               @NotNull ModelFactory factory)
      throws MalformedCourseConfigurationFileException {
    JSONArray modulesJsonArray;
    try {
      modulesJsonArray = jsonObject.getJSONArray("modules");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(source,
          "Missing or malformed \"modules\" key", ex);
    }

    List<Module> modules = new ArrayList<>();
    // Indexing loop used to simplify checking that each entry is a JSON object.
    for (int i = 0; i < modulesJsonArray.length(); ++i) {
      try {
        JSONObject moduleObject = modulesJsonArray.getJSONObject(i);
        modules.add(Module.fromJsonObject(moduleObject, factory));
      } catch (JSONException ex) {
        throw new MalformedCourseConfigurationFileException(source,
            "\"modules\" value should be an array of objects containing module information", ex);
      } catch (MalformedURLException ex) {
        throw new MalformedCourseConfigurationFileException(source,
            "Malformed URL in module object", ex);
      }
    }
    return modules;
  }

  @NotNull
  private static Map<Long, Map<String, String>> getCourseExerciseModules(@NotNull JSONObject object,
                                                                         @NotNull String source)
      throws MalformedCourseConfigurationFileException {
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
      throw new MalformedCourseConfigurationFileException(source,
          "Malformed \"exerciseModules\" object", e);
    }
  }

  @NotNull
  private static Map<String, String> getCourseRequiredPlugins(@NotNull JSONObject jsonObject,
                                                              @NotNull String source)
      throws MalformedCourseConfigurationFileException {
    HashMap<String, String> requiredPlugins = new HashMap<>();
    JSONObject requiredPluginsJson;
    try {
      requiredPluginsJson = jsonObject.getJSONObject("requiredPlugins");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(source,
          "Missing or malformed \"requiredPlugins\" key", ex);
    }
    Iterable<String> keys = requiredPluginsJson::keys;
    for (String pluginId : keys) {
      try {
        String pluginName = requiredPluginsJson.getString(pluginId);
        requiredPlugins.put(pluginId, pluginName);
      } catch (JSONException ex) {
        throw new MalformedCourseConfigurationFileException(source,
            "Expected id-name-pairs in requiredPlugins object", ex);
      }
    }
    return requiredPlugins;
  }

  @NotNull
  private static Map<String, URL> getCourseResourceUrls(@NotNull JSONObject jsonObject,
                                                        @NotNull String source)
      throws MalformedCourseConfigurationFileException {
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
        throw new MalformedCourseConfigurationFileException(source,
            "Expected name-url-pairs in \"resources\" object", ex);
      }
    }
    return resourceUrls;
  }

  @NotNull
  private static List<String> getCourseAutoInstallComponentNames(@NotNull JSONObject jsonObject,
                                                                 @NotNull String source)
      throws MalformedCourseConfigurationFileException {
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
        throw new MalformedCourseConfigurationFileException(
            source, "Names in \"autoInstall\" array should be course components", e);
      }
    }
    return autoInstallComponentNames;
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
  public Points getSubmissionsDashboard() {
    return submissionResultsList;
  }

  public void setSubmissionsDashboard(@NotNull Points submissionResultsList) {
    this.submissionResultsList = submissionResultsList;
  }
}
