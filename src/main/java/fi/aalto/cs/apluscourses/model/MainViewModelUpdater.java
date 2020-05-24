package fi.aalto.cs.apluscourses.model;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;

import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.utils.CoursesClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jetbrains.annotations.NotNull;

public class MainViewModelUpdater {

  @NotNull
  private MainViewModelProvider mainViewModelProvider;

  @NotNull
  private Project project;

  private long updateInterval;

  /**
   * Construct a {@link MainViewModelUpdater} with the given {@link MainViewModelProvider}, project,
   * and update interval.
   * @param mainViewModelProvider The {@link MainViewModelProvider} from which the main view model
   *                              to be updated is gotten.
   * @param project               The project to which this updater is tied.
   * @param updateInterval        The interval at which the updater performs the update.
   */
  public MainViewModelUpdater(@NotNull MainViewModelProvider mainViewModelProvider,
                              @NotNull Project project,
                              @NotNull long updateInterval) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.project = project;
    this.updateInterval = updateInterval;
  }

  /**
   * Construct a {@link MainViewModelUpdater} with the given project.
   * @param project The project to which thsi updater is tied.
   */
  public MainViewModelUpdater(@NotNull Project project) {
    this(PluginSettings.getInstance(), project, PluginSettings.MAIN_VIEW_MODEL_UPDATE_INTERVAL);
  }

  /**
   * This method attempts to update the main view model repeatedly, sleeping a given update interval
   * time between attempts. If the project of this updater is closed, then this method returns. This
   * method is intended to be run on a background thread.
   */
  public void run() {
    try {
      while (true) {
        URL courseUrl = ReadAction.compute(() -> {
          if (project.isDisposed() || !project.isOpen()) {
            // This updater is no longer needed.
            throw new InterruptedException();
          }
          try {
            return new URL(PluginSettings.COURSE_CONFIGURATION_FILE_URL); // TODO
          } catch (MalformedURLException e) {
            // Fail silently and stop this updater.
            throw new InterruptedException();
          }
        });

        try {
          // We don't need to be inside the read action when we parse the course configuration file.
          // This way we avoid long read actions, which block the UI thread from making write
          // actions and lead to UI freezes. We just have to remember to check once again that the
          // project is still actually open.
          InputStream inputStream = CoursesClient.fetchJson(courseUrl);
          Course course = Course.fromConfigurationData(
              new InputStreamReader(inputStream),
              PluginSettings.COURSE_CONFIGURATION_FILE_URL,
              new IntelliJModelFactory(project));
          ReadAction.run(() -> {
            if (project.isDisposed() || !project.isOpen()) {
              // The project closed while we parsed the course configuration file, so we throw
              // the result away and stop this updater.
              throw new InterruptedException();
            }
            mainViewModelProvider
                .getMainViewModel(project)
                .courseViewModel
                .set(new CourseViewModel(course));
          });
        } catch (IOException | MalformedCourseConfigurationFileException e) {
          // Fail silently and go back to sleep. If the internet connection was down for an example,
          // then we'll succeed when we try again after the sleeping interval.
        }

        Thread.sleep(updateInterval);
      }
    } catch (InterruptedException e) {
      // Do nothing, this updater is done.
    }
  }
}
