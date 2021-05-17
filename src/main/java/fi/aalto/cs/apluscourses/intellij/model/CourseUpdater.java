package fi.aalto.cs.apluscourses.intellij.model;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.notification.Notification;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NewModulesVersionsNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.Version;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CourseUpdater extends RepeatedTask {

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

  private final Set<String> notifiedModules = ConcurrentHashMap.newKeySet();

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
    super(updateInterval);
    this.course = course;
    this.project = project;
    this.courseUrl = courseUrl;
    this.configurationFetcher = configurationFetcher;
    this.eventToTrigger = eventToTrigger;
    this.notifier = notifier;
  }

  /**
   * Construct a course updater with reasonable defaults.
   */
  public CourseUpdater(@NotNull Course course,
                       @NotNull Project project,
                       @NotNull URL courseUrl,
                       @NotNull Event eventToTrigger) {
    this(course, project, courseUrl, CoursesClient::fetch, eventToTrigger, new DefaultNotifier(),
        PluginSettings.UPDATE_INTERVAL);
  }

  @Override
  protected void doTask() {
    var progressViewModel =
        PluginSettings.getInstance().getMainViewModel(project).progressViewModel;
    var progress =
            progressViewModel.start(1, getText("ui.ProgressBarView.refreshingModules"), false);
    updateModules(fetchModulesInfo());
    if (Thread.interrupted()) {
      progress.finish();
      return;
    }
    notifyUpdatableModules();
    progress.finish();
    eventToTrigger.trigger();
  }

  private Map<URI, ModuleInfo> fetchModulesInfo() {
    try {
      var inputStream = configurationFetcher.fetch(courseUrl);
      var tokenizer = new JSONTokener(inputStream);
      var object = new JSONObject(tokenizer);
      var array = object.getJSONArray("modules");
      // The equals and hashCode methods of the URL class can cause DNS lookups, so URI instances
      // are preferred in maps.
      Map<URI, ModuleInfo> mapping = new HashMap<>();
      for (int i = 0; i < array.length(); ++i) {
        var module = array.getJSONObject(i);
        var url = new URL(module.getString("url"));
        var moduleInfo = new ModuleInfo(Version.fromString(module.optString("version", "1.0")),
            module.optString("changelog", ""));
        mapping.put(urlToUri(url), moduleInfo);
      }
      return mapping;
    } catch (IOException | JSONException e) {
      return Collections.emptyMap();
    }
  }

  private void updateModules(@NotNull Map<URI, ModuleInfo> uriToModuleInfo) {
    for (var module : course.getModules()) {
      var moduleInfo = uriToModuleInfo.get(urlToUri(module.getUrl()));
      if (moduleInfo != null) {
        module.updateVersion(moduleInfo.getVersion());
        module.updateChangelog(moduleInfo.getChangelog());
      }
    }
  }

  private void notifyUpdatableModules() {
    var updatableModules = course.getUpdatableModules()
        .stream()
        .filter(m -> !m.hasLocalChanges() || m.isMajorUpdate())
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

  private class ModuleInfo {
    @NotNull
    private final Version version;

    @NotNull
    private final String changelog;

    public ModuleInfo(@NotNull Version version,
                      @NotNull String changelog) {
      this.version = version;
      this.changelog = changelog;
    }

    @NotNull
    public Version getVersion() {
      return version;
    }

    @NotNull
    public String getChangelog() {
      return changelog;
    }
  }

}
