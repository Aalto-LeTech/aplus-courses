package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Event;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;


public class Tutorial {
  private List<Task> tasks = new ArrayList<>();

  @NotNull
  public final Event tutorialUpdated;

  public Tutorial() { //default constructor for testing purposes
    tasks.add(new Task());
    Task second = new Task("editorOpen", "GoodStuff/o1/goodstuff/gui/GoodStuff.scala");
    Task third = new Task("editorOpen", "GoodStuff/o1/goodstuff/Category.scala");
    third.setLastTask(true);
    tasks.add(second);
    tasks.add(third);
    this.tutorialUpdated = new Event();
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public Task getNextTask(@NotNull Task task) {
    if (tasks.indexOf(task) != tasks.size() - 1) {
      return tasks.get(tasks.indexOf(task) + 1);
    }
    return null;
  }

  public void setCompleted() {
    this.tutorialUpdated.trigger();
  }

}
