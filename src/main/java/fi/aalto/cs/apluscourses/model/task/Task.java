package fi.aalto.cs.apluscourses.model.task;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.utils.Event;
import org.jetbrains.annotations.NotNull;

public abstract class Task {

  private final String action;

  @NotNull
  public final Event taskUpdated;

  /**
   * Task constructor. Action and file are Strings
   * read from the configuration file.
   * @param action the action to be performed
   */
  protected Task(String action) {
    this.action = action;
    this.taskUpdated = new Event();
  }

  /**
   * Empty Task constructor.
   */
  protected Task() {
    this("editorOpen");
  }

  public String getAction() {
    return action;
  }

  /**
   * Sets the current Task as complete and also triggers an Event
   * that calls the method currentTaskCompleted in TutorialViewModel.
   */
  public void setComplete() {
    taskUpdated.trigger();
  }

  public abstract void endTask();

  public abstract boolean startTask(Project project);
}

