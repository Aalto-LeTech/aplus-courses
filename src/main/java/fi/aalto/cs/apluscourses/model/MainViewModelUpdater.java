package fi.aalto.cs.apluscourses.model;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;

import fi.aalto.cs.apluscourses.intellij.model.APlusProject;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainViewModelUpdater {

  @NotNull
  private MainViewModelProvider mainViewModelProvider;

  @NotNull
  private APlusProject aplusProject;

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
    this.aplusProject = new APlusProject(project);
    this.updateInterval = updateInterval;
  }

  /**
   * Construct a {@link MainViewModelUpdater} with the given project.
   * @param project The project to which thsi updater is tied.
   */
  public MainViewModelUpdater(@NotNull Project project) {
    this(PluginSettings.getInstance(), project, PluginSettings.MAIN_VIEW_MODEL_UPDATE_INTERVAL);
  }

  private void interruptIfProjectClosed() throws InterruptedException {
    if (aplusProject.getProject().isDisposed() || !aplusProject.getProject().isOpen()) {
      throw new InterruptedException();
    }
  }

  @NotNull
  URL getCourseUrl() throws InterruptedException {
    return ReadAction.compute(() -> {
      interruptIfProjectClosed();
      try {
        URL url = aplusProject.getCourseFileUrl();
        if (url == null) {
          throw new InterruptedException();
        }
        return url;
      } catch (IOException e) {
        throw new InterruptedException();
      }
    });
  }

  @Nullable
  Course getCourse(@NotNull URL courseUrl) {
    // We don't need to be inside the read action when we parse the course configuration file.
    // This way we avoid long read actions, which block the UI thread from making write
    // actions and lead to UI freezes. We just have to remember to later check again that the
    // project is still actually open.
    try {
      return Course.fromUrl(courseUrl, new IntelliJModelFactory(aplusProject.getProject()));
    } catch (IOException | MalformedCourseConfigurationFileException e) {
      return null;
    }
  }

  @NotNull
  Set<String> getProjectModules() throws InterruptedException {
    com.intellij.openapi.module.Module[] projectModules = ReadAction.compute(() -> {
      interruptIfProjectClosed();
      return aplusProject.getModuleManager().getModules();
    });
    return Arrays.stream(projectModules)
        .map(module -> module.getName())
        .collect(Collectors.toSet());
  }

  @NotNull
  List<Module> getUpdatableModules(@NotNull Course course) throws InterruptedException {
    List<Module> updatableModules = new ArrayList<>();

    // Modules listed in the course file may have been removed from the project, so we have to check
    // that the modules are still in the project.
    Set<String> projectModules = getProjectModules();

    // Minor concurrency issue: we make the list of updatable modules outside of a read action, so
    // (however unlikely) a module may be removed from the project in the meantime. This shouldn't
    // be a big issue however, as it would just lead to a update notification for a module that has
    // been removed.

    Map<String, String> localModuleIds;
    try {
      localModuleIds = aplusProject.getCourseFileModuleIds();
    } catch (IOException e) {
      return updatableModules;
    }

    for (Module module : course.getModules()) {
      // An updatable module must be in the project and it's ID in the local
      // course file must be different from the ID in the course configuration file.
      if (!projectModules.contains(module.getName())) {
        continue;
      }
      if (!module.getVersionId().equals(localModuleIds.get(module.getName()))) {
        updatableModules.add(module);
      }
    }

    return updatableModules;
  }

  /**
   * This method attempts to update the main view model repeatedly, sleeping a given update interval
   * time between attempts. If the project of this updater is closed, then this method returns. This
   * method is intended to be run on a background thread.
   */
  public void run() {
    try {
      while (true) {
        URL courseUrl = getCourseUrl();
        Course course = getCourse(courseUrl);
        // If parsing the course configuration file fails, then we just silently go back to sleep.
        // For an example, if the internet connection was down, then we may succeed when we try
        // again after sleeping.
        if (course != null) {
          // TODO: what do we actually do with the list of updatable modules? Notify the user?
          List<Module> updatableModules = getUpdatableModules(course);
          // The project may have closed while we parsed the course configuration file and computed
          // the updatable modules, in which case we throw the result away and stop this updater.
          ReadAction.run(this::interruptIfProjectClosed);
          mainViewModelProvider
              .getMainViewModel(aplusProject.getProject())
              .courseViewModel
              .set(new CourseViewModel(course));
        }
        Thread.sleep(updateInterval); // Good night :)
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
