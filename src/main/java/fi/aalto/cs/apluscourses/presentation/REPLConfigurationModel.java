package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class REPLConfigurationModel {

  private Project project;
  private String workingDirectory;
  private String targetModuleName;
  private List<String> modules;

  public static boolean showREPLConfigWindow = true;

  public REPLConfigurationModel(Project project, String workingDirectory,
      String targetModuleName) {
    this.project = project;
    this.workingDirectory = workingDirectory;
    this.targetModuleName = targetModuleName;
    this.modules = Arrays.stream(ModuleManager.getInstance(project).getModules())
        .filter(module -> {
          String name = module.getModuleTypeName();
          return name != null && name.equals("JAVA_MODULE");
        })
        .map(Module::getName)
        .collect(Collectors.toList());
  }

  public List<String> getModules() {
    return modules;
  }

  public void setModules(List<String> modules) {
    this.modules = modules;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public String getTargetModuleName() {
    return targetModuleName;
  }

  public void setTargetModuleName(String targetModuleName) {
    this.targetModuleName = targetModuleName;
  }
}
