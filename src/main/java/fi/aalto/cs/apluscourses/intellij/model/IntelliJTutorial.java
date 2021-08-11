package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.IoErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.TaskNotifier;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentInstallerImpl;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.Tutorial;
import fi.aalto.cs.apluscourses.model.task.Task;
import fi.aalto.cs.apluscourses.ui.ReinstallDialogs;
import fi.aalto.cs.apluscourses.utils.JsonUtil;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class IntelliJTutorial extends Tutorial {

  private final Notifier notifier;

  /**
   * A constructor.
   */
  public IntelliJTutorial(Task @NotNull [] tasks,
                          String @NotNull [] moduleDependencies,
                          boolean downloadDependencies,
                          boolean deleteDependencies) {
    super(tasks, moduleDependencies, downloadDependencies, deleteDependencies);
    this.notifier = new DefaultNotifier();
  }

  /**
   * Construct a tutorial instance from the given JSON object.
   */
  public static IntelliJTutorial fromJsonObject(@NotNull JSONObject jsonObject) {
    return new IntelliJTutorial(JsonUtil.parseArray(jsonObject.getJSONArray("tasks"),
        JSONArray::getJSONObject, Task::fromJsonObject, Task[]::new),
        JsonUtil.parseArray(jsonObject.getJSONArray("moduleDependencies"),
            JSONArray::getString, String::new, String[]::new),
        jsonObject.optBoolean("downloadDependencies", false),
        jsonObject.optBoolean("deleteDependencies", false));
  }

  @Override
  public void downloadDependencies(@NotNull Course course, @NotNull Project project) {
    var taskNotifier = new TaskNotifier(notifier, project);
    taskNotifier.notifyDownloadingDeps(false);

    var modules = getModules(course);

    for (var module : modules) {
      if (module.hasLocalChanges()) {
        ((Module) module).setForceUpdatable();
      }
    }

    var componentInstallerFactory = new ComponentInstallerImpl.FactoryImpl<>(new SimpleAsyncTaskManager());
    var dialogs = new ReinstallDialogs(project);

    componentInstallerFactory.getInstallerFor(course, dialogs).installAsync(modules,
        () -> taskNotifier.notifyDownloadingDeps(true));
  }

  @Override
  public void deleteDependencies(@NotNull Course course, @NotNull Project project) {
    for (var component : getModules(course)) {
      try {
        component.remove();
        component.unload();
      } catch (IOException e) {
        notifier.notify(new IoErrorNotification(e), project);
      }
    }
  }

  @Override
  public boolean dependenciesMissing(@NotNull Project project) {
    var taskNotifier = new TaskNotifier(notifier, project);
    var moduleSource = new ProjectModuleSource();
    var missing = Arrays
        .stream(getModuleDependencies())
        .filter(name -> moduleSource.getModule(project, name) == null)
        .collect(Collectors.toList());
    missing.forEach(taskNotifier::notifyMissingModule);
    return !missing.isEmpty();
  }

  private List<Component> getModules(@NotNull Course course) {
    return Arrays
        .stream(getModuleDependencies())
        .map(course::getComponentIfExists)
        .filter(Module.class::isInstance)
        .map(Module.class::cast)
        .map(module -> (Component) module.copy("Ideact_" + module.getOriginalName()))
        .collect(Collectors.toList());
  }

}
