package fi.aalto.cs.apluscourses.presentation.ideactivities;

import fi.aalto.cs.apluscourses.model.task.OpenFileTask;
import fi.aalto.cs.apluscourses.model.task.Task;

public class TaskPresentationUtils {

  private TaskPresentationUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Method for getting the html description for a specific
   * Task that will be used in TaskView.
   * @param task the Task to generate the description for
   * @return the relevant description for this Task
   */
  public static String getTaskDescription(Task task) {
    if (task instanceof OpenFileTask) {
      OpenFileTask openFileTask = (OpenFileTask) task;
      return "<html>" + openFileTask.getAction() + "<br>"
        + openFileTask.getFile() + "</html>";
    }
    return "";
  }
}
