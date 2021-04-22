package fi.aalto.cs.apluscourses.presentation.ideactivities;

import fi.aalto.cs.apluscourses.model.task.Task;

public class TaskViewModelFactory {

  /**
   * Factory Method that generates the related TaskViewModel
   * for the given Task.
   * @param task the Task to generate the TaskViewModel for
   * @return the new instance of TaskViewModel
   */
  public TaskViewModel getTaskViewModel(Task task) {
    switch (task.getAction()) {
      case "editorOpen":
        return new OpenFileTaskViewModel(task);
      default:
        return null;
    }
  }
}
