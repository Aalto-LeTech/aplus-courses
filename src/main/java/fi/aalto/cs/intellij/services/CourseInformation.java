package fi.aalto.cs.intellij.services;

import com.intellij.openapi.components.ServiceManager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseInformation {
  private static final Logger logger = LoggerFactory
      .getLogger(CourseInformation.class);

  private String courseName;
  private Map<String, URL> moduleUrls;

  public CourseInformation() {
    // Replace this with the correct path when testing with a local course configuration file.
    parse("o1.json");
  }

  /**
   * The CourseInformation instance managed by the service manager.
   */
  public static CourseInformation getInstance() {
    return ServiceManager.getService(CourseInformation.class);
  }

  /**
   * Parses the given course configuration file.
   * @param pathToCourseConfig The path to the course configuration file.
   */
  public void parse(String pathToCourseConfig) {
    logger.info("Parsing the course configuration file '{}'", pathToCourseConfig);
    courseName = "";
    moduleUrls = new HashMap<>();
    FileReader file;
    try {
      file = new FileReader(pathToCourseConfig);
    } catch (FileNotFoundException e) {
      // TODO: notify the user of the error with a notification
      logger.error("Failed to find course configuration file '{}'", pathToCourseConfig);
      return;
    }

    JSONTokener tokenizer = new JSONTokener(file);
    JSONObject jsonObj = new JSONObject(tokenizer);
    try {
      courseName = jsonObj.getString("name");

      JSONObject modules = jsonObj.getJSONObject("modules");
      Iterator<String> it = modules.keys();
      while (it.hasNext()) {
        String key = it.next();
        String urlString = modules.getJSONObject(key).getString("url");
        URL moduleUrl;
        try {
          moduleUrl = new URL(urlString);
        } catch (MalformedURLException e) {
          // TODO: notify the user of the error with a notification
          logger.error("Invalid url '{}' in course configuration file", urlString);
          return;
        }
        moduleUrls.put(key, moduleUrl);
      }
    } catch (JSONException e) {
      // TODO: notify the user of the error with a notification
      logger.error("Invalid JSON in course configuration file: {}", e.getMessage());
    }
  }

  /**
   * Returns the name of the currently loaded course.
   */
  public String getCourseName() {
    return courseName;
  }

  /**
   * Returns the names of the code modules for the currently loaded course.
   */
  public List<String> getModuleNames() {
    List<String> moduleNames = new ArrayList<String>();
    moduleNames.addAll(moduleUrls.keySet());
    return moduleNames;
  }

  /**
   * Returns the URL where the code module is located.
   */
  public URL getModuleUrl(String moduleName) {
    return moduleUrls.get(moduleName);
  }

}
