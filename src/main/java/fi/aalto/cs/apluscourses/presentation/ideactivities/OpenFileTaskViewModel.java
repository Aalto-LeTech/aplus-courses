package fi.aalto.cs.apluscourses.presentation.ideactivities;

import fi.aalto.cs.apluscourses.model.task.OpenFileTask;
import fi.aalto.cs.apluscourses.model.task.Task;
import org.jetbrains.annotations.NotNull;

public class OpenFileTaskViewModel implements TaskViewModel {

  private final OpenFileTask openFileTask;

  public OpenFileTaskViewModel(Task task) {
    this.openFileTask = (OpenFileTask) task;
  }

  @NotNull
  @Override
  public String getTaskDescription() {
    return  "<html>" + openFileTask.getAction() + "<br>"
        + openFileTask.getFile() + "</html>";
  }
}
