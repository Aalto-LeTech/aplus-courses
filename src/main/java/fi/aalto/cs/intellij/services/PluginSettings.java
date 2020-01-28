package fi.aalto.cs.intellij.services;

import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.serviceContainer.NonInjectable;

import fi.aalto.cs.intellij.common.Course;
import fi.aalto.cs.intellij.common.MalformedCourseConfigurationFileException;
import fi.aalto.cs.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.intellij.notifications.Notifier;

import java.io.FileNotFoundException;

import org.jetbrains.annotations.NotNull;

public class PluginSettings {
  @NotNull
  private Course currentlyLoadedCourse;
  @NotNull
  private final Notifier notifier;

  /**
   * Constructs a plugin settings instance with the default course configuration file.
   */
  public PluginSettings() {
    this(Notifications.Bus::notify);
  }

  /**
   * Constructs a plugin settings instance with the default course configuration file and the given
   * notifier.
   * @param notifier The notifier used to notify the user of various events.
   */
  @NonInjectable
  public PluginSettings(@NotNull Notifier notifier) {
    this.notifier = notifier;
    try {
      // Replace this with the correct path when testing with a local course configuration file.
      currentlyLoadedCourse = Course.fromConfigurationFile("o1.json");
    } catch (FileNotFoundException | MalformedCourseConfigurationFileException e) {
      currentlyLoadedCourse = Course.createEmptyCourse();
      notifier.notify(new CourseConfigurationError(e.getMessage()), null);
    }
  }

  @NotNull
  public static PluginSettings getInstance() {
    return ServiceManager.getService(PluginSettings.class);
  }

  @NotNull
  public Course getCurrentlyLoadedCourse() {
    return currentlyLoadedCourse;
  }
}
