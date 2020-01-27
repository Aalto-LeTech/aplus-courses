package fi.aalto.cs.intellij.services;

import com.intellij.openapi.components.ServiceManager;

import fi.aalto.cs.intellij.common.Course;
import fi.aalto.cs.intellij.common.MalformedCourseConfigurationFileException;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginSettings {
  private static final Logger logger = LoggerFactory.getLogger(PluginSettings.class);

  private Course currentlyLoadedCourse;

  /**
   * Constructs a plugin settings instance with the default course configuration file.
   */
  public PluginSettings() {
    try {
      // Replace this with the correct path when testing with a local course configuration file.
      currentlyLoadedCourse = Course.fromConfigurationFile("o1.json");
      // TODO: error handling, perhaps a notification to the user
    } catch (FileNotFoundException e) {
      logger.info("Failed to find course configuration file", e);
    } catch (MalformedCourseConfigurationFileException e) {
      logger.info("Malformed course configuration file", e);
    }
  }

  public static PluginSettings getInstance() {
    return ServiceManager.getService(PluginSettings.class);
  }

  public Course getCurrentlyLoadedCourse() {
    return currentlyLoadedCourse;
  }
}
