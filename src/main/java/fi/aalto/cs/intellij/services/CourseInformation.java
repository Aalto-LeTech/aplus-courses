package fi.aalto.cs.intellij.services;

import com.intellij.openapi.components.ServiceManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseInformation {
  private static final Logger logger = LoggerFactory
      .getLogger(CourseInformation.class);

  private String courseName;
  private Map<String, URL> moduleUrls;

  public CourseInformation() {
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
    logger.info("Parsing course configuration file");
    courseName = "O1";
    moduleUrls = new HashMap<String, URL>();

    // TODO: actually read this stuff from the config file
    try {
      moduleUrls.put("O1Library", new URL("https://example.com"));
      moduleUrls.put("GoodStuff", new URL("https://example.com"));
    } catch (MalformedURLException e) {
      logger.error("Invalid URL in course configuration file: " + e.getMessage());
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

}
