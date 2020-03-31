package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
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

  //todo this goes into course settings once they exist
  public static boolean showREPLConfigWindow = true;

  /**
   * Creates a model for {@link ReplConfigurationForm}.
   *
   * @param project a {@link Project} to extract {@link Module}s from
   * @param moduleWorkingDirectory a path to the workDir of the {@link Module} in focus
   * @param targetModuleName a {@link String} name of the {@link Module} in focus
   */
  public ReplConfigurationFormModel(
      Project project,
      String moduleWorkingDirectory,
      String targetModuleName) {
    this.project = project;
    this.moduleWorkingDirectory = moduleWorkingDirectory;
    this.targetModuleName = targetModuleName;
    Module[] modules = ModuleManager.getInstance(project).getModules();
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
          String name = module.getModuleTypeName();
//        Scala modules are of a "JAVA_MODULE" type,
//        so it the way to distinct them from SBT-built ones.
          return name != null && name.equals("JAVA_MODULE");
        })
        .map(Module::getName)
        .collect(Collectors.toList());
  }

  public List<String> getModuleNames() {
    return moduleNames;
  }

  public void setModuleNames(List<String> moduleNames) {
    this.moduleNames = moduleNames;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
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
