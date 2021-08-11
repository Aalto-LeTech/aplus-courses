package fi.aalto.cs.apluscourses.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.Task;
import fi.aalto.cs.apluscourses.utils.Event;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class Tutorial {
  private final List<Task> tasks;
  private final String @NotNull [] moduleDependencies;
  private final boolean downloadDependencies;
  private final boolean deleteDependencies;

  @NotNull
  public final Event tutorialCompleted = new Event();

  /**
   * A constructor.
   */
  protected Tutorial(Task @NotNull [] tasks,
                     String @NotNull [] moduleDependencies,
                     boolean downloadDependencies,
                     boolean deleteDependencies) {
    this.tasks = List.of(tasks);
    this.moduleDependencies = moduleDependencies;
    this.downloadDependencies = downloadDependencies;
    this.deleteDependencies = deleteDependencies;
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
  public abstract void downloadDependencies(@NotNull Course course, @NotNull Project project);

  /**
   * Deletes the module deps for a tutorial.
   */
  public abstract void deleteDependencies(@NotNull Course course, @NotNull Project project);


  /**
   * Returns true and shows notifications if modules are missing.
   */
  public abstract boolean dependenciesMissing(@NotNull Project project);

  public String @NotNull [] getModuleDependencies() {
    return moduleDependencies;
  }

  public boolean isDownloadDependencies() {
    return downloadDependencies;
  }

  public boolean isDeleteDependencies() {
    return deleteDependencies;
  }
}
