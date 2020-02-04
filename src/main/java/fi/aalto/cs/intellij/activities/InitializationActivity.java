package fi.aalto.cs.intellij.activities;

import com.intellij.notification.Notifications;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.intellij.common.Course;
import fi.aalto.cs.intellij.common.IntelliJCourseFactory;
import fi.aalto.cs.intellij.common.MalformedCourseConfigurationFileException;
import fi.aalto.cs.intellij.common.ResourceException;
import fi.aalto.cs.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.intellij.notifications.Notifier;
import fi.aalto.cs.intellij.presentation.CourseModel;
import fi.aalto.cs.intellij.services.PluginSettings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitializationActivity implements StartupActivity, DumbAware {
  private static final Logger logger = LoggerFactory.getLogger(InitializationActivity.class);

  @NotNull
  private final Notifier notifier;

  public InitializationActivity() {
    this(Notifications.Bus::notify);
  }

  public InitializationActivity(@NotNull Notifier notifier) {
    this.notifier = notifier;
  }

  @Override
  public void runActivity(@NotNull Project project) {
    Course course;
    try {
      course = Course.fromResource(PluginSettings.COURSE_CONFIGURATION_FILE_PATH,
          new IntelliJCourseFactory(project));
    } catch (ResourceException | MalformedCourseConfigurationFileException e) {
      course = null;
      logger.info("Error occurred while trying to parse a course configuration file", e);
      notifier.notify(new CourseConfigurationError(e), null);
    }
    PluginSettings.getInstance().getMainModel().course.set(
        course == null ? null : new CourseModel(course));
  }
}
