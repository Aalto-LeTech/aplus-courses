package fi.aalto.cs.intellij.services;

import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;

import fi.aalto.cs.intellij.common.Course;
import fi.aalto.cs.intellij.common.MalformedCourseConfigurationFileException;
import fi.aalto.cs.intellij.notifications.CourseConfigurationError;

import java.io.FileNotFoundException;

public class PluginSettings {
  private Course currentlyLoadedCourse;

  /**
   * Constructs a plugin settings instance with the default course configuration file.
   */
  public PluginSettings() {
    try {
      // Replace this with the correct path when testing with a local course configuration file.
      currentlyLoadedCourse = Course.fromConfigurationFile("o1.json");
    } catch (FileNotFoundException | MalformedCourseConfigurationFileException e) {
      Notifications.Bus.notify(new CourseConfigurationError(e.getMessage()));
    }
  }

  public static PluginSettings getInstance() {
    return ServiceManager.getService(PluginSettings.class);
  }

  public Course getCurrentlyLoadedCourse() {
    return currentlyLoadedCourse;
  }
}
