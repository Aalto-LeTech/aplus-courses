package fi.aalto.cs.apluscourses.intellij.activities;

import com.intellij.notification.Notifications;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.RequiredPluginsCheckerAction;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJCourseFactory;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.DataUtil;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationFileException;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.utils.ResourceException;
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
    if (course != null) {
      PluginSettings.getInstance()
          .getMainViewModel(project).courseViewModel.set(new CourseViewModel(course));
      ActionUtil.launch(RequiredPluginsCheckerAction.ACTION_ID,
          DataUtil.extendDataContext(project, null));
    }
  }
}
