package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.model.task.OpenFileTask;
import fi.aalto.cs.apluscourses.model.task.Task;
import fi.aalto.cs.apluscourses.utils.Event;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;


public class Tutorial {
  private List<Task> tasks = new ArrayList<>();

  @NotNull
  public final Event tutorialUpdated;

  /**
   * Empty constructor used to initialize
   * a Tutorial object with default values.
   */
  public Tutorial() { //default constructor for testing purposes
    tasks.add(new OpenFileTask("GoodStuff/o1/goodstuff/gui/CategoryDisplayWindow.scala"));
    Task second = new OpenFileTask("GoodStuff/o1/goodstuff/gui/GoodStuff.scala");
    Task third = new OpenFileTask("GoodStuff/o1/goodstuff/Category.scala");
    tasks.add(second);
    tasks.add(third);
    this.tutorialUpdated = new Event();
  }

  public List<Task> getTasks() {
    return tasks;
  }

  /**
   * Method to get the next Task in row.
   * @param task the current Task whose successor we are looking for.
   * @return the next Task to be performed
   */
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
