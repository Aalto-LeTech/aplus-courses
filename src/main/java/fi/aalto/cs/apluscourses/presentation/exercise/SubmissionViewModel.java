package fi.aalto.cs.apluscourses.presentation.exercise;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmittableExercise;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.CalledWithReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmissionViewModel {

  private SubmittableExercise exercise;

  private List<Group> groups;

  private APlusAuthentication authentication;

  private Project project;

  private Module module;

  /**
   * Construct a submission view model with the given exercise, groups, authentication, and project.
   */
  public SubmissionViewModel(@NotNull SubmittableExercise exercise,
                             @NotNull List<Group> groups,
                             @NotNull APlusAuthentication authentication,
                             @NotNull Project project
                             ) {
    this.exercise = exercise;
    this.groups = groups;
    this.authentication = authentication;
    this.project = project;
  }

  @NotNull
  public Project getProject() {
    return project;
  }

  /**
   * Get the names of the modules available for this submission (i.e. the modules of the project).
   */
  @CalledWithReadLock
  public List<String> getAvailableModuleNames() {
    return Arrays
        .stream(ModuleManager.getInstance(project).getModules())
        .map(Module::getName)
        .collect(Collectors.toList());
  }

  @Nullable
  public Module getModule() {
    return module;
  }

  /**
   * Set the selected module for this submission to the module with the given name.
   */
  @CalledWithReadLock
  public void setModule(@NotNull String moduleName) {
    this.module = Arrays
        .stream(ModuleManager.getInstance(project).getModules())
        .filter(module -> moduleName.equals(module.getName()))
        .findAny()
        .orElse(null);
  }

}
