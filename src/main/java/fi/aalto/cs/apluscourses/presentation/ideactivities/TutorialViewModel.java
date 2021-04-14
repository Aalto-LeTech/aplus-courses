package fi.aalto.cs.apluscourses.presentation.ideactivities;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.ui.ideactivities.StartTutorialDialog;
import fi.aalto.cs.apluscourses.model.Task;
import fi.aalto.cs.apluscourses.model.Tutorial;
import fi.aalto.cs.apluscourses.ui.ideactivities.TaskView;
import fi.aalto.cs.apluscourses.intellij.utils.ActivitiesListener;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class TutorialViewModel {
  //TODO functionality to stop the Tutorial?

  @NotNull
  public final ObservableProperty<TaskViewModel> taskViewModel =
      new ObservableReadWriteProperty<>(null);

  private final Project project;
  private Task currentTask;
  private final List<Task> tasks;
  private final Tutorial tutorial;

  public TutorialViewModel(@NotNull Tutorial tutorial, @Nullable Project project) {
    this.tutorial = tutorial;
    this.tasks = tutorial.getTasks();
    if (!tasks.isEmpty()) {
      this.currentTask = tasks.get(0);
    }
    this.project = project;
  }

  public void startTutorial() {
    synchronized (lock) {
      StartTutorialDialog startTutorialDialog = new StartTutorialDialog(this, project);
      int result = startTutorialDialog.display();
      if (result == JOptionPane.OK_OPTION) { //Make more UI agnostic
        callback.show(new TaskViewModel(currentTask));

        currentTask.taskUpdated.addListener(
            this, TutorialViewModel::currentTaskCompleted);

        ActivitiesListener.createListener(currentTask, project);
      }
    }
  }

  public void currentTaskCompleted() {
    synchronized (lock) {
      currentTask.taskUpdated.removeCallback(this);
      currentTask = tutorial.getNextTask(currentTask);
      if (currentTask != null) { // perhaps refine and use a field like isLastTask and not a null check
        callback.show(new TaskViewModel(currentTask));
        currentTask.taskUpdated.addListener(
            this, TutorialViewModel::currentTaskCompleted);
        ActivitiesListener.createListener(currentTask, project);
      } else {
        //A dialog will summarize what data will be sent, it might be better to separate
        //the logic (gathering that info) into a separate ViewModel initialized here.
        tutorial.setCompleted();
        // gather the data here..
      }
    }
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public Task getCurrentTask() {
    return this.currentTask;
  }
}
