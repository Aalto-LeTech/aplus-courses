package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.ResourceException;
import fi.aalto.cs.apluscourses.utils.Resources;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Course implements ModuleSource {
  @NotNull
  private final String name;

  @NotNull
  private final List<Module> modules;

  // Maps ids of required plugins to the names of required plugins.
  @NotNull
  private final Map<String, String> requiredPlugins;

  /**
   * Constructs a course with the given parameters. Course objects are usually created using
   * {@link Course#fromConfigurationFile}
   *
   * @param name            The name of the course
   * @param modules         The list of modules in the course.
   * @param requiredPlugins A map containing the required plugins for this course. The keys are the
   *                        ids of the plugins and the values are the names of the plugins.
   */
  public Course(@NotNull String name, @NotNull List<Module> modules,
                @NotNull Map<String, String> requiredPlugins) {
    this.name = name;
    this.modules = modules;
    this.requiredPlugins = requiredPlugins;
  }

  /**
   * Returns an "empty" course, that is, a course with no name, no modules, and no required plugins.
   * @return An empty course.
   */
  public static Course createEmptyCourse() {
    return new Course("", new ArrayList<>(), new HashMap<>());
  }

  public static Course fromResource(@NotNull String resourceName, @NotNull ModelFactory factory)
      throws ResourceException, MalformedCourseConfigurationFileException {
    Reader reader = new InputStreamReader(Resources.DEFAULT.getStream(resourceName));
    return fromConfigurationData(reader, resourceName, factory);
  }

  /**
   * Parses the course configuration file located at the given path.
   *
   * @param pathToCourseConfig The path to the course configuration file.
   * @return A course instance containing the information parsed from the configuration file.
   * @throws FileNotFoundException                     If the configuration file can't be found with
   *                                                   the given path.
   * @throws MalformedCourseConfigurationFileException If the configuration file is malformed in
   *                                                   any way.
   */
  @NotNull
  public static Course fromConfigurationFile(@NotNull String pathToCourseConfig,
                                             @NotNull ModelFactory factory)
      throws FileNotFoundException, MalformedCourseConfigurationFileException {
    FileReader fileReader = new FileReader(pathToCourseConfig);
    return fromConfigurationData(fileReader, pathToCourseConfig, factory);
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
    String courseName = getCourseName(jsonObject, sourcePath);
    List<Module> courseModules = getCourseModules(jsonObject, sourcePath, factory);
    Map<String, String> requiredPlugins
        = getCourseRequiredPlugins(jsonObject, sourcePath);
    return factory.createCourse(courseName, courseModules, requiredPlugins);
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
   * {@link Course#fromConfigurationFile}, then the modules are returned in the order in which they
   * are listed in the course configuration file.
   *
   * @return All modules of this course.
   */
  @NotNull
  public List<Module> getModules() {
    return Collections.unmodifiableList(modules);
  }

  /**
   * Returns the URL from which the module with the given name can be fetched.
   *
   * @param moduleName             The name of the module.
   * @return                       The URL from which the module can be fetched.
   * @throws NoSuchModuleException if the course doesn't contain a module with the given name.
   */
  @NotNull
  public URL getModuleUrl(String moduleName) throws NoSuchModuleException {
    Optional<Module> matchingModule = modules
        .stream()
        .filter(module -> module.getName().equals(moduleName))
        .findFirst();
    return matchingModule
        .orElseThrow(() -> new NoSuchModuleException(this, moduleName, null))
        .getUrl();
  }


  /**
   * Returns a map containing the required plugins for the course. The keys are the ids of the
   * plugins and the values are the names corresponding to the ids.
   *
   * @return A map with the required plugins of the course.
   */
  @NotNull
  public Map<String, String> getRequiredPlugins() {
    return requiredPlugins;
  }

  @NotNull
  private static JSONObject getCourseJsonObject(@NotNull Reader reader, @NotNull String path)
      throws MalformedCourseConfigurationFileException {
    JSONTokener tokenizer = new JSONTokener(reader);
    try {
      return new JSONObject(tokenizer);
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(path,
          "Course configuration file should consist of a valid JSON object", ex);
    }
  }

  @NotNull
  private static String getCourseName(@NotNull JSONObject jsonObject, @NotNull String path)
      throws MalformedCourseConfigurationFileException {
    try {
      return jsonObject.getString("name");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(path,
          "Missing or malformed \"name\" key", ex);
    }
  }

  @NotNull
  private static List<Module> getCourseModules(JSONObject jsonObject,
                                               String path,
                                               @NotNull ModelFactory factory)
      throws MalformedCourseConfigurationFileException {
    JSONArray modulesJsonArray;
    try {
      modulesJsonArray = jsonObject.getJSONArray("modules");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(path,
          "Missing or malformed \"modules\" key", ex);
    }

    List<Module> modules = new ArrayList<>();
    // Indexing loop used to simplify checking that each entry is a JSON object.
    for (int i = 0; i < modulesJsonArray.length(); ++i) {
      try {
        JSONObject moduleObject = modulesJsonArray.getJSONObject(i);
        modules.add(Module.fromJsonObject(moduleObject, factory));
      } catch (JSONException ex) {
        throw new MalformedCourseConfigurationFileException(path,
            "\"modules\" value should be an array of objects containing module information", ex);
      } catch (MalformedURLException ex) {
        throw new MalformedCourseConfigurationFileException(path,
            "Malformed URL in module object", ex);
      }
    }
    return modules;
  }

  @NotNull
  private static Map<String, String> getCourseRequiredPlugins(@NotNull JSONObject jsonObject,
                                                              @NotNull String path)
      throws MalformedCourseConfigurationFileException {
    HashMap<String, String> requiredPlugins = new HashMap<>();
    JSONObject requiredPluginsJson;
    try {
      requiredPluginsJson = jsonObject.getJSONObject("requiredPlugins");
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(path,
          "Missing or malformed \"requiredPlugins\" key", ex);
    }
    Iterable<String> keys = requiredPluginsJson::keys;
    for (String pluginId : keys) {
      try {
        String pluginName = requiredPluginsJson.getString(pluginId);
        requiredPlugins.put(pluginId, pluginName);
      } catch (JSONException ex) {
        throw new MalformedCourseConfigurationFileException(path,
            "Expected id-name-pairs in requiredPlugins object", ex);
      }
    }
    return requiredPlugins;
  }


  @Override
  @Nullable
  public Module getModule(String moduleName) {
    return modules
        .stream()
        .filter(module -> module.getName().equals(moduleName))
        .findFirst()
        .orElse(null);
  }
}
