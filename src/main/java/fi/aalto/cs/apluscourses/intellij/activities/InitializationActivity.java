package fi.aalto.cs.apluscourses.intellij.activities;

import static fi.aalto.cs.apluscourses.intellij.services.PluginSettings.MODULE_REPL_INITIAL_COMMANDS_FILE_NAME;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity.Background;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationException;
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
    this(new DefaultNotifier());
  }

  public InitializationActivity(@NotNull Notifier notifier) {
    this.notifier = notifier;
  }

  @Override
  public void runActivity(@NotNull Project project) {
    PluginSettings pluginSettings = PluginSettings.getInstance();
    pluginSettings.initializeLocalIdeSettings();
    pluginSettings.ignoreFileInProjectView(MODULE_REPL_INITIAL_COMMANDS_FILE_NAME, project);

    URL courseConfigurationFileUrl = getCourseUrlFromProject(project);
    if (courseConfigurationFileUrl == null) {
      return;
    }

    Course course;
    try {
      course = Course.fromUrl(courseConfigurationFileUrl, new IntelliJModelFactory(project));
    } catch (UnexpectedResponseException | MalformedCourseConfigurationException e) {
      logger.error("Error occurred while trying to parse a course configuration file", e);
      notifier.notify(new CourseConfigurationError(e), project);
      return;
    } catch (IOException e) {
      logger.info("IOException occurred while using the HTTP client", e);
      notifier.notify(new NetworkErrorNotification(e), project);
      return;
    }
    var courseProject = new CourseProject(course, courseConfigurationFileUrl, project);
    PluginSettings.getInstance().registerCourseProject(courseProject);
  }

  @Nullable
  private static URL getCourseUrlFromProject(@NotNull Project project) {
    if (project.isDefault()) {
      return null;
    }

    try {
      boolean isCourseProject = PluginSettings
          .getInstance()
          .getCourseFileManager(project)
          .load();
      if (isCourseProject) {
        return PluginSettings.getInstance().getCourseFileManager(project).getCourseUrl();
      } else {
        return null;
      }
    } catch (IOException | JSONException e) {
      logger.error("Malformed project course tag file", e);
      return null;
    }
  }
}
