package fi.aalto.cs.apluscourses.presentation;

import com.intellij.notification.Notification;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.notifications.NewModulesVersionsNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainViewModelUpdater {

  @NotNull
  private final MainViewModel mainViewModel;

  @NotNull
  private final Project project;

  private final long updateInterval;

  @NotNull
  private final Notifier notifier;
  @NotNull
  private final PasswordStorage.Factory passwordStorageFactory;

  private Thread thread;

  private Notification newModulesVersionsNotification = null;

  private final Set<String> notifiedModules = new HashSet<>();

  /**
   * Construct a {@link MainViewModelUpdater} with the given {@link MainViewModel}, project, and
   * update interval.
   *
   * @param mainViewModel  The {@link MainViewModel} that gets updated.
   * @param project        The project to which this updater is tied.
   * @param updateInterval The interval at which the updater performs the update.
   */
  public MainViewModelUpdater(@NotNull MainViewModel mainViewModel,
                              @NotNull Project project,
                              long updateInterval,
                              @NotNull Notifier notifier,
                              @NotNull PasswordStorage.Factory passwordStorageFactory) {
    this.mainViewModel = mainViewModel;
    this.project = project;
    this.updateInterval = updateInterval;
    this.notifier = notifier;
    this.passwordStorageFactory = passwordStorageFactory;
    this.thread = new Thread(this::run);
  }

  private static int prevents = 0;
  private static final Object preventsLock = new Object();

  /*
  TODO: the following for methods shouldn't be static. MainViewModelUpdater instances are project
  specific, however there are some issues with how CourseProjectAction first starts auto-installs
  and only then creates the main view model updater, which means prevent can't be used by the
  auto-installs in a project-specific way. This is why these methods are static and prevent all
  instances of MainViewModelUpdater from running. This should be improved in the future.
   */

  /**
   * Prevents any main view model updater from running until {@link MainViewModelUpdater#enable}
   * gets called. If a main view model update is occurring when this method gets called, then this
   * method blocks until the update has completed. If this method is called multiple times, then
   * each call needs a corresponding call to {@link MainViewModelUpdater#enable} before a main view
   * model update can occur.
   */
  public static void prevent() {
    synchronized (preventsLock) {
      try {
        while (prevents == -1) {
          preventsLock.wait();
        }
        ++prevents;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * See {@link MainViewModelUpdater#prevent}. This method must never get called unless {@link
   * MainViewModelUpdater#prevent} has been called at least once.
   */
  public static void enable() {
    synchronized (preventsLock) {
      --prevents;
      if (prevents == 0) {
        preventsLock.notifyAll();
      }
    }
  }

  /**
   * Waits until there are no prevents, and then disables prevents until {@link
   * MainViewModelUpdater#updateDone} is called.
   */
  private static void beginUpdate() {
    try {
      synchronized (preventsLock) {
        while (prevents != 0) {
          preventsLock.wait();
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    prevents = -1;
  }

  private static void updateDone() {
    synchronized (preventsLock) {
      prevents = 0;
      preventsLock.notifyAll();
    }
  }

  @NotNull
  private URL getCourseUrl() {
    return ReadAction.compute(() -> {
      if (project.isDisposed() || !project.isOpen()) {
        return null;
      }
      return PluginSettings
          .getInstance()
          .getCourseFileManager(project)
          .getCourseUrl();
    });
  }

  @Nullable
  private Course getCourse(@Nullable URL courseUrl) {
    if (courseUrl == null) {
      return null;
    }

    try {
      return ReadAction.compute(
          () -> Course.fromUrl(courseUrl, new IntelliJModelFactory(project)));
    } catch (Exception e) {
      return null;
    }
  }

  private void updateMainViewModel(@Nullable Course newCourse) {
    if (newCourse == null) {
      return;
    }

    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    if (courseViewModel != null) {
      Course current = courseViewModel.getModel();
      current.unregister();
    }

    mainViewModel.courseViewModel.set(new CourseViewModel(newCourse));

    PasswordStorage passwordStorage = passwordStorageFactory.create(newCourse.getApiUrl());
    TokenAuthentication.Factory authenticationFactory =
        APlusTokenAuthentication.getFactoryFor(passwordStorage);
    mainViewModel.readAuthenticationFromStorage(passwordStorage, authenticationFactory);

    notifyNewVersions(newCourse);

    newCourse.register();
  }

  protected List<Module> filterOldUpdatableModules(List<Module> modules) {
    return modules.stream()
            .filter(m -> notifiedModules.add(m.getName()))
            .collect(Collectors.toList());
  }

  private void notifyNewVersions(Course newCourse) {
    if (newModulesVersionsNotification != null) {
      newModulesVersionsNotification.expire();
      newModulesVersionsNotification = null;
    }
    List<Module> updatableModules = filterOldUpdatableModules(newCourse.getUpdatableModules());
    if (!updatableModules.isEmpty()) {
      newModulesVersionsNotification = new NewModulesVersionsNotification(updatableModules);
      notifier.notify(newModulesVersionsNotification, project);
    }
  }

  /**
   * This method attempts to update the main view model repeatedly, sleeping a given update interval
   * time between attempts. If the project of this updater is closed, then this method returns. This
   * method is intended to be run on a background thread.
   */
  private void run() {
    try {
      // Sonar dislikes infinite loops...
      while (true) { //NOSONAR

        // Wait until there are no prevents and then disable prevents.
        beginUpdate();

        URL courseUrl = getCourseUrl();
        Course course = getCourse(courseUrl);
        // If parsing the course configuration file fails, then we just silently go back to sleep.
        // For an example, if the internet connection was down, then we may succeed when we try
        // again after sleeping.
        updateMainViewModel(course);

        // Enable prevents again.
        updateDone();

        Thread.sleep(updateInterval); // Good night :)
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Starts this updater on a separate thread. The updater repeatedly updates its main view model,
   * sleeping the given amount of time between update attempts.
   */
  public void start() {
    thread.start();
  }

  /**
   * Interrupts the updater and starts it again. This can be used to trigger an instant main view
   * model update, skipping the sleep interval.
   */
  public synchronized void restart() {
    thread.interrupt();
    thread = new Thread(this::run);
    thread.start();
  }

  /**
   * Interrupts this updater.
   */
  public void interrupt() {
    thread.interrupt();
  }
}
