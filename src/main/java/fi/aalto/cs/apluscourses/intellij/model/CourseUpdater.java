package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.Event;
import java.net.URL;
import java.util.Map;
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

  private final long updateInterval;

  private Thread thread;

  /**
   * Construct a course updater with the given parameters.
   */
  public CourseUpdater(@NotNull Course course,
                       @NotNull Project project,
                       @NotNull URL courseUrl,
                       @NotNull Event eventToTrigger,
                       long updateInterval) {
    this.course = course;
    this.project = project;
    this.courseUrl = courseUrl;
    this.updateInterval = updateInterval;
    this.eventToTrigger = eventToTrigger;
    this.thread = null;
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
    var urlsToModules = createUrlToModuleMapping(newCourse);
    for (var module : course.getModules()) {
      var newModule = urlsToModules.get(module.getUrl());
      if (newModule != null) {
        module.updateVersionId(newModule.getVersionId());
      }
    }
  }

  private static Map<URL, Module> createUrlToModuleMapping(@NotNull Course course) {
    return course.getModules()
        .stream()
        .collect(Collectors.toMap(Module::getUrl, Function.identity()));
  }

}
