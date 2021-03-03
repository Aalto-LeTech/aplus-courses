package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.notification.Notification;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NewModulesVersionsNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.Event;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CourseUpdater {

  public interface CourseConfigurationFetcher {
    InputStream fetch(URL configurationUrl) throws IOException;
  }

  @NotNull
  private final Course course;

  @NotNull
  private final Project project;

  @NotNull
  private final URL courseUrl;

  @NotNull
  private final CourseConfigurationFetcher configurationFetcher;

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
                       @NotNull CourseConfigurationFetcher configurationFetcher,
                       @NotNull Event eventToTrigger,
                       @NotNull Notifier notifier,
                       long updateInterval) {
    this.course = course;
    this.project = project;
    this.courseUrl = courseUrl;
    this.configurationFetcher = configurationFetcher;
    this.eventToTrigger = eventToTrigger;
    this.notifier = notifier;
    this.updateInterval = updateInterval;
  }

  /**
   * Construct a course updater with reasonable defaults.
   */
  public CourseUpdater(@NotNull Course course,
                       @NotNull Project project,
                       @NotNull URL courseUrl,
                       @NotNull Event eventToTrigger) {
    this(course, project, courseUrl, CoursesClient::fetch, eventToTrigger, new DefaultNotifier(),
        PluginSettings.COURSE_UPDATE_INTERVAL);
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
          updateModuleIds(fetchModuleIds());
          notifyUpdatableModules();
          eventToTrigger.trigger();
          Thread.sleep(updateInterval); //  NOSONAR
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  private Map<URI, String> fetchModuleIds() {
    try {
      var inputStream = configurationFetcher.fetch(courseUrl);
      var tokenizer = new JSONTokener(inputStream);
      var object = new JSONObject(tokenizer);
      var array = object.getJSONArray("modules");
      // The equals and hashCode methods of the URL class can cause DNS lookups, so URI instances
      // are preferred in maps.
      Map<URI, String> mapping = new HashMap<>();
      for (int i = 0; i < array.length(); ++i) {
        var module = array.getJSONObject(i);
        var url = new URL(module.getString("url"));
        var id = module.getString("id");
        mapping.put(urlToUri(url), id);
      }
      return mapping;
    } catch (IOException | JSONException e) {
      return Collections.emptyMap();
    }
  }

  private void updateModuleIds(@NotNull Map<URI, String> uriToModuleId) {
    for (var module : course.getModules()) {
      var id = uriToModuleId.get(urlToUri(module.getUrl()));
      if (id != null) {
        module.updateVersionId(id);
      }
    }
  }

  private void notifyUpdatableModules() {
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
  }

  @Nullable
  private static URI urlToUri(@NotNull URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      return null;
    }
  }

}
