package fi.aalto.cs.apluscourses.presentation.ideactivities;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.utils.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.Task;
import fi.aalto.cs.apluscourses.model.Tutorial;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TutorialViewModel {

  private final Project project;
  private Task currentTask;
  private final List<Task> tasks;
  private final Tutorial tutorial;
  private final TaskCallback callback;
  private final Object lock = new Object();

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

  public void startNextTask() {
    synchronized (lock) {
      callback.show(new TaskViewModel(currentTask));
      currentTask.taskUpdated.addListener(
          this, TutorialViewModel::currentTaskCompleted);
      ActivitiesListener.createListener(currentTask, project);
    }
  }

  //TODO gap in logic if the Task is somehow already performed.
  //in this case file already open -> Display message that the
  //condition was already met and to repeat the action?

  public void currentTaskCompleted() {
    synchronized (lock) {
      currentTask.taskUpdated.removeCallback(this);
      if (!currentTask.isLastTask()) {
        currentTask = tutorial.getNextTask(currentTask);
        startNextTask();
      } else {
        //A dialog will summarize what data will be sent, it might be better to separate
        //the logic into a separate ViewModel initialized here.
        // gather the data here..
        System.out.println("Tutorial is done!");
        tutorial.setCompleted();
      }
    }
  }

  public void cancelTutorial() {
    synchronized (lock) {
      currentTask.getListener().unregisterListener();
      currentTask.taskUpdated.removeCallback(this);
      tasks.forEach(task -> task.setIsCompleted(false));
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
