package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.notification.Notification;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.NewModulesVersionsNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.Event;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseUpdater {

  @NotNull
  private final Course course;

  @NotNull
  private final Project project;

  @NotNull
  private final URL courseUrl;

  @NotNull
  private final Event eventToTrigger;

  @NotNull
  private final Notifier notifier;

  private final long updateInterval;

  private Thread thread = null;

  private final Set<String> notifiedModules = new HashSet<>();

  private Notification moduleUpdatesNotification = null;

  /**
   * Construct a course updater with the given parameters.
   */
  public CourseUpdater(@NotNull Course course,
                       @NotNull Project project,
                       @NotNull URL courseUrl,
                       @NotNull Event eventToTrigger,
                       @NotNull Notifier notifier,
                       long updateInterval) {
    this.course = course;
    this.project = project;
    this.courseUrl = courseUrl;
    this.eventToTrigger = eventToTrigger;
    this.notifier = notifier;
    this.updateInterval = updateInterval;
  }

  /**
   * Starts or restarts this updater. This method is thread safe and can be called from multiple
   * threads.
   */
  public synchronized void restart() {
    if (thread != null) {
      thread.interrupt();
    }
    thread = new Thread(this::run);
    thread.start();
  }

  /*
   * The run lock ensures that only one thread executes the 'run' method. The run lock guarantees
   * that a new thread created by 'restart' blocks until the previous thread has received the
   * interrupt and exited. Note, that making the 'run' method 'synchronized' would cause a deadlock,
   * because 'restart' is synchronized as well.
   */
  private final Object runLock = new Object();

  private void run() {
    synchronized (runLock) {
      while (true) { //  NOSONAR
        try {
          var newCourse = fetchCourse();
          if (newCourse != null) {
            updateModuleIds(newCourse);
            eventToTrigger.trigger();
          }
          var updatableModules = course.getUpdatableModules()
              .stream()
              .filter(m -> notifiedModules.add(m.getName()))
              .collect(Collectors.toList());
          if (!updatableModules.isEmpty()) {
            if (moduleUpdatesNotification != null) {
              moduleUpdatesNotification.expire();
            }
            moduleUpdatesNotification = new NewModulesVersionsNotification(updatableModules);
            notifier.notifyAndHide(moduleUpdatesNotification, project);
          }
          Thread.sleep(updateInterval); // Good night :-)
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  @Nullable
  private Course fetchCourse() {
    try {
      // Read-lock needed, since we access the project
      return ReadAction.compute(() -> Course.fromUrl(courseUrl, new IntelliJModelFactory(project)));
    } catch (Exception e) {
      return null;
    }
  }

  private void updateModuleIds(@NotNull Course newCourse) {
    var urisToModules = createUriToModuleMapping(newCourse);
    for (var module : course.getModules()) {
      var newModule = urisToModules.get(urlToUri(module.getUrl()));
      if (newModule != null) {
        module.updateVersionId(newModule.getVersionId());
      }
    }
  }

  @Nullable
  private static URI urlToUri(@NotNull URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      return null;
    }
  }

  private static Map<URI, Module> createUriToModuleMapping(@NotNull Course course) {
    // hashCode and equals of URLs can cause DNS lookups, so URIs are preferred in maps
    return course.getModules()
        .stream()
        .filter(module -> urlToUri(module.getUrl()) != null)
        .collect(Collectors.toMap(module -> urlToUri(module.getUrl()), Function.identity()));
  }

}
