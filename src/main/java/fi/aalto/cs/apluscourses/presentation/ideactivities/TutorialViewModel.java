package fi.aalto.cs.apluscourses.presentation.ideactivities;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.utils.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.Task;
import fi.aalto.cs.apluscourses.model.Tutorial;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TutorialViewModel {

  private final Project project;
  private Task currentTask;
  private final List<Task> tasks;
  private final AtomicBoolean skipTask = new AtomicBoolean();
  private final Tutorial tutorial;
  private final TaskCallback callback;
  private final Object lock = new Object();

  /**
   * Constructor.
   * @param tutorial the Tutorial to be performed
   * @param taskCallback callback for when the tutorial is done
   * @param project the Project that the Tutorial will be in
   */
  public TutorialViewModel(@NotNull Tutorial tutorial,
                           @NotNull TaskCallback taskCallback, @Nullable Project project) {
    this.tutorial = tutorial;
    this.tasks = tutorial.getTasks();
    if (!tasks.isEmpty()) {
      this.currentTask = tasks.get(0);
    }
    this.project = project;
    this.callback = taskCallback;
  }

  /**
   * Begins the nextTask which is indicated by the currentTask variable.
   */
  public void startNextTask() {
    synchronized (lock) {
      currentTask.taskUpdated.addListener(
          this, TutorialViewModel::currentTaskCompleted);
      currentTask.alreadyComplete.addListener(this, TutorialViewModel::alreadyComplete);
      ActivitiesListener.createListener(currentTask, project);
      if (!skipTask.getAndSet(false) && currentTask != null) {
        callback.show(new TaskViewModel(currentTask));
      }
      // The Task/Tutorial has been completed prematurely
      // becuase the Activity was already performed.
      // E.g. the file was open already, variable renamed etc.
      // No need to show the instructions for this Task
      // as we are proceeding directly to the next one.
      // We can instead inform the user that they have already completed it
      // through the alreadyComplete() method.

    }
  }

  /**
   * Sets the currentTask as completed and fress up any resources associated with it.
   * If this task was the last one the Tutorial is completed,
   * if not, then the currentTask is set to point to the next Task to be done.
   */
  public void currentTaskCompleted() {
    synchronized (lock) {
      currentTask.taskUpdated.removeCallback(this);
      currentTask.alreadyComplete.removeCallback(this);
      if (!currentTask.equals(tasks.get(tasks.size() - 1))) {
        currentTask = tutorial.getNextTask(currentTask);
        startNextTask();
      } else {
        System.out.println("Tutorial is done!");
        currentTask = null;
        tutorial.setCompleted();
      }
    }
  }

  /**
   * Functionality for canceling the Tutorial, mainly frees up resources
   * and resetes the tasks as not completed.
   */
  public void cancelTutorial() {
    synchronized (lock) {
      currentTask.getListener().unregisterListener();
      currentTask.taskUpdated.removeCallback(this);
      currentTask.alreadyComplete.removeCallback(this);
      tasks.forEach(task -> task.setIsComplete(false));
    }
  }

  /**
   * This method is called if the Task's action was already
   * complete when starting the Task. The Task's instruction window
   * is not shown (skipped) and we proceed with the rest of the Tutorial.
   */
  public void alreadyComplete() {
    synchronized (lock) {
      System.out.println("Already Done" + currentTask.getFile());
      skipTask.set(true);
      currentTaskCompleted();
    }
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public Task getCurrentTask() {
    return this.currentTask;
  }

  public interface TaskCallback {
    void show(TaskViewModel viewModel);
  }

}
