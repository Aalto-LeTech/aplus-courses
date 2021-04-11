package fi.aalto.cs.apluscourses.presentation.ideactivities;

import fi.aalto.cs.apluscourses.model.Task;

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

}
