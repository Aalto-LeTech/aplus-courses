package fi.aalto.cs.apluscourses.presentation.ideactivities;

import fi.aalto.cs.apluscourses.model.task.Task;
import org.jetbrains.annotations.NotNull;

public class TaskViewModel {

  private Task task;

  public TaskViewModel(Task task) {
    this.task = task;
  }

  public String getAction() {
    return task.getAction();
  }

  public Task getTask() {
    return task;
  }

  @NotNull
  public String getTaskDescription() {
    return TaskPresentationUtils.getTaskDescription(task);
  }

}
