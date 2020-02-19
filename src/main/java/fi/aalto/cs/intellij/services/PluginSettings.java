package fi.aalto.cs.intellij.services;

import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.serviceContainer.NonInjectable;

import fi.aalto.cs.intellij.common.Course;
import fi.aalto.cs.intellij.common.MalformedCourseConfigurationFileException;
import fi.aalto.cs.intellij.common.UnexpectedResponseException;
import fi.aalto.cs.intellij.notifications.ClientIoError;
import fi.aalto.cs.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.intellij.notifications.Notifier;
import fi.aalto.cs.intellij.utils.CoursesClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginSettings {
  private static final Logger logger = LoggerFactory
      .getLogger(PluginSettings.class);

  @NotNull
  private final Course currentlyLoadedCourse;
  @NotNull
  private final Notifier notifier;

  /**
   * Constructs a plugin settings instance with the default course configuration file URL.
   */
  public PluginSettings() throws MalformedURLException {
    this(new URL("https://grader.cs.hut.fi/static/O1_2020/projects/o1_course_config.json"),
        Notifications.Bus::notify);
  }

  /**
   * Constructs a plugin settings instance with the default course configuration file and the given
   * notifier.
   * @param notifier The notifier used to notify the user of various events.
   */
  @NonInjectable
  public PluginSettings(@NotNull String courseConfigurationFilePath, @NotNull Notifier notifier) {
    this.notifier = notifier;
    Course course;
    try {
      course = Course.fromConfigurationFile(courseConfigurationFilePath);
    } catch (FileNotFoundException | MalformedCourseConfigurationFileException e) {
      course = Course.createEmptyCourse();
      logger.info("Error occurred while trying to parse a course configuration file", e);
      notifier.notify(new CourseConfigurationError(e), null);
    }
    currentlyLoadedCourse = course;
  }

  /**
   * Constructs a plugin settings instance with the given notifier and the course loaded from the
   * course configuration file at the given URL.
   * @param notifier The notifier used to notify the user of various events.
   */
  @NonInjectable
  public PluginSettings(@NotNull URL courseConfigurationFileUrl, @NotNull Notifier notifier) {
    Course course;
    this.notifier = notifier;
    try {
      InputStream inputStream = CoursesClient.fetchJson(courseConfigurationFileUrl);
      course = Course.fromConfigurationData(new InputStreamReader(inputStream));
    } catch (MalformedCourseConfigurationFileException | UnexpectedResponseException ex) {
      course = Course.createEmptyCourse();
      logger.info("Error occurred while trying to parse a course configuration file", ex);
      notifier.notify(new CourseConfigurationError(ex), null);
    } catch (IOException ex) {
      /*
       * ex instanceof HttpHostConnectException => server could be down
       * ex instanceof UnknownHostException => user is potentially lacking internet connection
       */
      course = Course.createEmptyCourse();
      logger.info("IOException occurred while using the HTTP client", ex);
      notifier.notify(new ClientIoError(ex), null);
    }
    currentlyLoadedCourse = course;
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
