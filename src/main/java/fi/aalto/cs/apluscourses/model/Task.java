package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.utils.ActivitiesListener;
import fi.aalto.cs.apluscourses.utils.Event;
import org.jetbrains.annotations.NotNull;

public class Task {

  private String action;
  private String file;
  private boolean isCompleted;
  private ActivitiesListener listener;
  private boolean isLastTask;

  @NotNull
  public final Event taskUpdated;

  /**
   * Task constructor. Action and file are Strings
   * read from the configuration file.
   * @param action the action to be performed
   * @param file the file the action concerns
   */
  public Task(String action, String file) {
    this.action = action;
    this.file = file;
    this.taskUpdated = new Event();
  }

  /**
   * Empty Task constructor.
   */
  public Task() {
    action = "editor.open";
    file = "GoodStuff/o1/goodstuff/gui/CategoryDisplayWindow.scala";
    this.taskUpdated = new Event();
  }

  public String getAction() {
    return action;
  }

  public String getFile() {
    return file;
  }

  public void setCompleted() {
    this.isCompleted = true;
    taskUpdated.trigger();
  }

  public void setIsCompleted(boolean isCompleted) {
    this.isCompleted = isCompleted;
  }

  public boolean isCompleted() {
    return isCompleted;
  }

  public void setListener(ActivitiesListener listener) {
    this.listener = listener;
  }

  public ActivitiesListener getListener() {
    return this.listener;
  }

  public boolean isLastTask() {
    return isLastTask;
  }

  public void setLastTask(boolean lastTask) {
    isLastTask = lastTask;
  }
}

