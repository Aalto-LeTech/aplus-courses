package fi.aalto.cs.apluscourses.presentation.ideactivities;

import fi.aalto.cs.apluscourses.model.task.Task;
import org.jetbrains.annotations.NotNull;

public class TaskViewModel {

  private final @NotNull Task task;

  public TaskViewModel(@NotNull Task task) {
    this.task = task;
  }

  public @NotNull String getTaskDescription() {
    return "<html>" + task.getAction() + "</html>";
  }
}
