package fi.aalto.cs.apluscourses.intellij.activities;

import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity.Background;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.RequiredPluginsCheckerAction;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.CourseFileManager;
import fi.aalto.cs.apluscourses.intellij.utils.ExtendedDataContext;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationFileException;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import java.io.IOException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitializationActivity implements Background {

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
    PluginSettings.getInstance().initializeLocalIdeSettings();

    URL courseConfigurationFileUrl = getCourseUrlFromProject(project);
    if (courseConfigurationFileUrl == null) {
      return;
    }

    try {
      Course.fromUrl(courseConfigurationFileUrl, new IntelliJModelFactory(project));
    } catch (UnexpectedResponseException | MalformedCourseConfigurationFileException e) {
      logger.error("Error occurred while trying to parse a course configuration file", e);
      notifier.notify(new CourseConfigurationError(e), null);
      return;
    } catch (IOException e) {
      logger.info("IOException occurred while using the HTTP client", e);
      notifier.notify(new NetworkErrorNotification(e), null);
      return;
    }
    PluginSettings.getInstance().createUpdatingMainViewModel(project);
    ActionUtil.launch(RequiredPluginsCheckerAction.ACTION_ID,
        new ExtendedDataContext().withProject(project));
    
  }

  @Nullable
  private static URL getCourseUrlFromProject(@NotNull Project project) {
    if (project.isDefault()) {
      return null;
    }

    try {
      boolean isCourseProject = CourseFileManager.getInstance().load(project);
      if (isCourseProject) {
        return CourseFileManager.getInstance().getCourseUrl();
      } else {
        return null;
      }
    } catch (IOException | JSONException e) {
      logger.error("Malformed project course tag file", e);
      return null;
    }
  }
}
