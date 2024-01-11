package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.ui.repl.ReplConfigurationForm;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ReplConfigurationFormModel {

  private Project project;
  private String moduleWorkingDirectory;
  private String targetModuleName;
  private List<String> moduleNames;
  private boolean startRepl = true;

  /**
   * Creates a model for {@link ReplConfigurationForm}.
   *
   * @param project                a {@link Project} to extract {@link Module}s from
   * @param moduleWorkingDirectory a path to the workDir of the {@link Module} in focus
   * @param targetModuleName       a {@link String} name of the {@link Module} in focus
   */
  public ReplConfigurationFormModel(
      @NotNull Project project,
      @NotNull String moduleWorkingDirectory,
      @NotNull String targetModuleName) {
    this.project = project;
    this.moduleWorkingDirectory = moduleWorkingDirectory;
    this.targetModuleName = targetModuleName;
    Module[] modules = getModules(project);
    this.moduleNames = getScalaModuleNames(modules);
  }

  /**
   * Filters out the names of Scala modules for the {@link Module} array.
   *
   * @param modules {@link Module} array to process
   * @return a {@link List} of {@link String} for names of the modules
   */
  @NotNull
  public static List<String> getScalaModuleNames(@NotNull Module[] modules) {
    return Arrays.stream(modules)
        .filter(module -> {
          // Java and Scala modules used to be called "JAVA_MODULE".
          // Then, it was changes to "Java Module". We will check for both.
          String moduleName = ModuleType.get(module).getName();
          return moduleName.equalsIgnoreCase("JAVA_MODULE") || moduleName.equalsIgnoreCase("Java Module");
        })
        .map(Module::getName)
        .collect(Collectors.toList());
  }

  /**
   * Method additionally to setting a {@link Project} updates the list of affiliated {@link Module}
   * names.
   *
   * @param project a {@link Project} to set and extract {@link Module}s to update the list of
   *                names.
   */
  public void setProject(Project project) {
    this.project = project;
    Module[] modules = getModules(project);
    this.moduleNames = getScalaModuleNames(modules);
  }

  /**
   * Method to extract {@link Module}s from {@link Project}.
   *
   * @param project a {@link Project} to extract {@link Module}s from
   * @return an array of {@link Module}
   */
  @NotNull
  private Module[] getModules(@NotNull Project project) {
    return ModuleManager.getInstance(project).getModules();
  }

  public List<String> getModuleNames() {
    return moduleNames;
  }

  public Project getProject() {
    return project;
  }

  public boolean isStartRepl() {
    return startRepl;
  }

  public void setStartRepl(boolean startRepl) {
    this.startRepl = startRepl;
  }

  public String getModuleWorkingDirectory() {
    return moduleWorkingDirectory;
  }

  public void setModuleWorkingDirectory(String moduleWorkingDirectory) {
    this.moduleWorkingDirectory = moduleWorkingDirectory;
  }

  public String getTargetModuleName() {
    return targetModuleName;
  }

  public void setTargetModuleName(String targetModuleName) {
    this.targetModuleName = targetModuleName;
  }
}
