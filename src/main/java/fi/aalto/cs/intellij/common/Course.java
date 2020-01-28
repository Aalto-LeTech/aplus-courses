package fi.aalto.cs.intellij.common;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Course {
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
  public static Course fromConfigurationFile(@NotNull String pathToCourseConfig)
      throws FileNotFoundException, MalformedCourseConfigurationFileException {
    JSONObject jsonObject = getCourseJsonObject(pathToCourseConfig);
    String courseName = getCourseName(jsonObject, pathToCourseConfig);
    List<Module> courseModules = getCourseModules(jsonObject, pathToCourseConfig);
    Map<String, String> requiredPlugins = getCourseRequiredPlugins(jsonObject, pathToCourseConfig);
    return new Course(courseName, courseModules, requiredPlugins);
  }


  /**
   * Returns the name of the course.
   *
   * @return The name of the course.
   */
  @NotNull
  String getName() {
    return name;
  }

  /**
   * Returns the names of all the modules in the course. If the course object is created with
   * {@link Course#fromConfigurationFile}, then the module names are returned in the order
   * in which they are listed in the course configuration file.
   */
  @NotNull
  public List<String> getModuleNames() {
    return modules
        .stream()
        .map(Module::getName)
        .collect(Collectors.toList());
  }

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
        .orElseThrow(() -> new NoSuchModuleException(this,
            "Course '" + name + "' has no module '" + moduleName + "'.", null))
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
  private static JSONObject getCourseJsonObject(@NotNull String path)
      throws FileNotFoundException, MalformedCourseConfigurationFileException {
    FileReader file = new FileReader(path);
    JSONTokener tokenizer = new JSONTokener(file);
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
  private static List<Module> getCourseModules(JSONObject jsonObject, String path)
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
      JSONObject moduleObject = modulesJsonArray.optJSONObject(i);
      if (moduleObject == null) {
        throw new MalformedCourseConfigurationFileException(path,
            "\"modules\" value should be an array of objects", null);
      }
      modules.add(getIndividualModule(moduleObject, path));
    }
    return modules;
  }

  @NotNull
  private static Module getIndividualModule(@NotNull JSONObject moduleObject, @NotNull String path)
      throws MalformedCourseConfigurationFileException {
    try {
      String moduleName = moduleObject.getString("name");
      URL moduleUrl = new URL(moduleObject.getString("url"));
      return new Module(moduleName, moduleUrl);
    } catch (JSONException ex) {
      throw new MalformedCourseConfigurationFileException(path,
          "Module objects should contain \"name\" and \"url\" keys with string values", ex);
    } catch (MalformedURLException ex) {
      throw new MalformedCourseConfigurationFileException(path,
          "Malformed URL in module object", ex);
    }
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


}
