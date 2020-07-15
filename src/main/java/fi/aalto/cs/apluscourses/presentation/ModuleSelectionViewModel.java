package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleSelectionViewModel {

  private final Project project;
  private final Module[] modules;
  private Module selectedModule;

  /**
   * Construct a module selection view model with the given available modules from the given
   * project.
   */
  public ModuleSelectionViewModel(@NotNull Module[] modules, @Nullable Project project) {
    this.project = project;
    this.modules = modules;
    this.selectedModule = null;
  }

  @Nullable
  public Project getProject() {
    return project;
  }

  /**
   * Return a list of names of the available modules.
   */
  @NotNull
  public List<String> getAvailableModuleNames() {
    return Arrays
        .stream(modules)
        .map(Module::getName)
        .collect(Collectors.toList());
  }

  @Nullable
  public Module getSelectedModule() {
    return selectedModule;
  }

  /**
   * Set the selected module of this view model to the module with the given name, or clear the
   * selection if no module with the given name is available.
   */
  public void setSelectedModule(@NotNull String moduleName) {
    this.selectedModule = Arrays
        .stream(modules)
        .filter(module -> moduleName.equals(module.getName()))
        .findAny()
        .orElse(null);
  }

  public void setSelectedModule(@NotNull Module module) {
    this.selectedModule = module;
  }

}
