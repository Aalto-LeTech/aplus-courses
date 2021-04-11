package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Event;
import org.jetbrains.annotations.NotNull;

public class Task {

  //subclasses with different constructos according to the action?
  private String instruction;
  private String action;
  private String file;
  private String content;
  private String assignmentId;
  private boolean isCompleted;
  private String oldName;
  private String newName;

  @NotNull
  public final Event taskUpdated;

  public Task(String instruction, String action, String file) {
    this.instruction = instruction;
    this.action = action;
    this.file = file;
    this.taskUpdated = new Event();
  }

  public Task() {
    instruction = "Find and open the file called CategoryDisplayWindow.scala";
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

}
