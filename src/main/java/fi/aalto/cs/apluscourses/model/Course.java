package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.ResourceException;
import fi.aalto.cs.apluscourses.utils.Resources;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Course implements ComponentSource {
  @NotNull
  private final String name;

  @NotNull
  private final List<Module> modules;

  @NotNull
  private final List<Library> libraries;

  // Maps ids of required plugins to the names of required plugins.
  @NotNull
  private final Map<String, String> requiredPlugins;

  /**
   * Constructs a course with the given parameters.
   *
   * @param name            The name of the course
   * @param modules         The list of modules in the course.
   * @param requiredPlugins A map containing the required plugins for this course. The keys are the
   *                        ids of the plugins and the values are the names of the plugins.
   */
  public Course(@NotNull String name,
                @NotNull List<Module> modules,
                @NotNull List<Library> libraries,
                @NotNull Map<String, String> requiredPlugins) {
    this.name = name;
    this.modules = modules;
    this.requiredPlugins = requiredPlugins;
    this.libraries = libraries;
  }

  public static Course fromResource(@NotNull String resourceName, @NotNull ModelFactory factory)
      throws ResourceException, MalformedCourseConfigurationFileException {
    Reader reader = new InputStreamReader(Resources.DEFAULT.getStream(resourceName));
    return fromConfigurationData(reader, resourceName, factory);
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
    return factory.createCourse(courseName, courseModules, Collections.emptyList(),
        requiredPlugins);
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
  @Override
  public Component getComponent(@NotNull String componentName) throws NoSuchComponentException {
    return Stream.concat(modules.stream(), libraries.stream())
        .filter(component -> component.getName().equals(componentName))
        .findFirst()
        .orElseThrow(() -> new NoSuchComponentException(componentName, null));
  }

  @Nullable
  @Override
  public Component getComponentIfExists(@NotNull String componentName) {
    return Stream.concat(modules.stream(), libraries.stream())
        .filter(component -> component.getName().equals(componentName))
        .findFirst()
        .orElse(null);
  }
}
