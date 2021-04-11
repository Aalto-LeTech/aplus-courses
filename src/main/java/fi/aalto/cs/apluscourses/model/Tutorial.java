package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Tutorial {
  private String name;
  private List<Task> tasks = new ArrayList<>();

  @NotNull
  public final Event tutorialUpdated;

  public Tutorial(String name, List<Task> tasks) {
    this.name = name;
    this.tasks = tasks;
    this.tutorialUpdated = new Event();
  }

  public Tutorial() { //default constructor for testing purposes
    this.name = "Assignment 1 (Tutorial)";
    tasks.add(new Task());
    Task second = new Task("Second Task", "editor.open", "GoodStuff/o1/goodstuff/gui/GoodStuff.scala");
    tasks.add(second);
    this.tutorialUpdated = new Event();
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public Task getNextTask(@NotNull Task task) {
    //control which is the currentTask
    if (tasks.indexOf(task) != tasks.size() - 1) {
      return tasks.get(tasks.indexOf(task) + 1);
    }
    return null;
  }

  public void setCompleted() {
    this.tutorialUpdated.trigger();
  }

}
