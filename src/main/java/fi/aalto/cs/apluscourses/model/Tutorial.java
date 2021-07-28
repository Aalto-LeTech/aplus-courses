package fi.aalto.cs.apluscourses.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.ProjectModuleSource;
import fi.aalto.cs.apluscourses.intellij.notifications.TaskNotifier;
import fi.aalto.cs.apluscourses.model.task.Task;
import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.JsonUtil;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;


public class Tutorial {
  private final List<Task> tasks;
  private final String @NotNull [] moduleDependencies;
  private final boolean downloadDependencies;
  private final boolean deleteDependencies;

  @NotNull
  public final Event tutorialCompleted = new Event();

  /**
   * A constructor.
   */
  public Tutorial(Task @NotNull [] tasks,
                  String @NotNull [] moduleDependencies,
                  boolean downloadDependencies,
                  boolean deleteDependencies) {
    this.tasks = List.of(tasks);
    this.moduleDependencies = moduleDependencies;
    this.downloadDependencies = downloadDependencies;
    this.deleteDependencies = deleteDependencies;
  }

  /**
   * Construct a tutorial instance from the given JSON object.
   */
  public static Tutorial fromJsonObject(@NotNull JSONObject jsonObject) {
    return new Tutorial(JsonUtil.parseArray(jsonObject.getJSONArray("tasks"),
        JSONArray::getJSONObject, Task::fromJsonObject, Task[]::new),
        JsonUtil.parseArray(jsonObject.getJSONArray("moduleDependencies"),
            JSONArray::getString, String::new, String[]::new),
        jsonObject.optBoolean("downloadDependencies", false),
        jsonObject.optBoolean("deleteDependencies", false));
  }

  public List<Task> getTasks() {
    return tasks;
  }

  /**
   * Method to get the next Task in row.
   *
   * @param task the current Task whose successor we are looking for.
   * @return the next Task to be performed
   */
  public @Nullable Task getNextTask(@NotNull Task task) {
    int index = tasks.indexOf(task);
    if (index != tasks.size() - 1) {
      return tasks.get(index + 1);
    }
    return null;
  }

  public void onComplete() {
    this.tutorialCompleted.trigger();
  }

  /**
   * Downloads the module deps for a tutorial, with the module name starting with "Ideact_".
   */
  public void downloadDependencies(@NotNull Course course,
                                   @NotNull Project project,
                                   @NotNull TaskNotifier taskNotifier) {
    taskNotifier.notifyDownloadingDeps(false);

    var modules = getModules(course);

    var componentInstallerFactory = new ComponentInstallerImpl.FactoryImpl<>(new SimpleAsyncTaskManager());
    var dialogs = new InstallerDialogs(project);

    componentInstallerFactory.getInstallerFor(course, dialogs).installAsync(modules,
        () -> taskNotifier.notifyDownloadingDeps(true));
  }

  /**
   * Deletes the module deps for a tutorial.
   */
  public void deleteDependencies(@NotNull Course course) {
    for (var component : getModules(course)) {
      try {
        component.remove();
      } catch (IOException e) {
        // fail silently
      }
    }
  }

  private List<Component> getModules(@NotNull Course course) {
    return Arrays
        .stream(moduleDependencies)
        .map(course::getComponentIfExists)
        .filter(Module.class::isInstance)
        .map(Module.class::cast)
        .map(module -> (Component) module.copy("Ideact_" + module.getOriginalName()))
        .collect(Collectors.toList());
  }

  /**
   * Returns true and shows notifications if modules are missing.
   */
  public boolean dependenciesMissing(@NotNull Project project,
                                    @NotNull TaskNotifier taskNotifier) {
    var moduleSource = new ProjectModuleSource();
    var missing = Arrays
        .stream(moduleDependencies)
        .filter(name -> moduleSource.getModule(project, name) == null)
        .collect(Collectors.toList());
    missing.forEach(taskNotifier::notifyMissingModule);
    return !missing.isEmpty();
  }

  public boolean isDownloadDependencies() {
    return downloadDependencies;
  }

  public boolean isDeleteDependencies() {
    return deleteDependencies;
  }
}
