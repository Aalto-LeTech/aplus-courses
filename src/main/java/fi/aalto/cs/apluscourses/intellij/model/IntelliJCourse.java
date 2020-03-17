package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Library;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.NoSuchModuleException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class IntelliJCourse extends Course {
  @NotNull
  private final Project project;
  @NotNull
  private final Map<String, IntelliJModule> modulesByNames;
  @NotNull
  private final CommonLibraryProvider commonLibraryProvider;

  public IntelliJCourse(@NotNull String name,
                        @NotNull List<Module> modules,
                        @NotNull List<Library> libraries,
                        @NotNull Map<String, String> requiredPlugins,
                        @NotNull Project project) {
    super(name, modules, libraries, requiredPlugins);

    this.project = project;
    this.commonLibraryProvider = new CommonLibraryProvider(project);
    this.modulesByNames = modules
        .stream()
        .map(IntelliJModule.class::cast)
        .collect(Collectors.toMap(IntelliJModule::getName, Function.identity()));
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  /**
   * Updates the states of the module objects when the given IntelliJ module is removed from the
   * IntelliJ project of this course.
   */
  public void onModuleRemove(@NotNull Module module) {
    module.stateMonitor.set(Module.UNLOADED);
  }

  /**
   * Updates the states of the module objects, when the directory with the files of the given module
   * are deleted.
   */
  public void onModuleFilesDeletion(@NotNull Module module) {
    module.stateMonitor.set(Module.UNINSTALLED);
  }

  @Nullable
  public IntelliJModule getModuleByName(String path) {
    return modulesByNames.get(path);
  }

  @NotNull
  @Override
  public Component getComponent(@NotNull String componentName) throws NoSuchModuleException {
    Module module = getModuleByName(componentName);
    if (module != null) {
      return module;
    }
    return commonLibraryProvider.getComponent(componentName);
  }
}
