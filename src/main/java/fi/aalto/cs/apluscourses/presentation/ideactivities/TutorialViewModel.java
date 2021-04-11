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

  @NotNull
  public final ObservableProperty<TaskViewModel> taskViewModel =
      new ObservableReadWriteProperty<>(null);

  private Project project;
  private Task currentTask;
  private List<Task> tasks;
  private Tutorial tutorial;

  public TutorialViewModel(@NotNull Tutorial tutorial, @Nullable Project project) {
    this.tutorial = tutorial;
    this.tasks = tutorial.getTasks();
    if (!tasks.isEmpty()) {
      this.currentTask = tasks.get(0);
    }
    this.project = project;
  }

  public void startTutorial() {
    StartTutorialDialog startTutorialDialog = new StartTutorialDialog(this, project);
    int result = startTutorialDialog.display();
    if (result == JOptionPane.OK_OPTION) { //Make more UI agnostic
      TaskView taskView = new TaskView();
      taskViewModel.addValueObserver(
         taskView, TaskView::viewModelChanged);
      taskViewModel.set(new TaskViewModel(currentTask));

      currentTask.taskUpdated.addListener(
          this, TutorialViewModel::currentTaskCompleted);

      ActivitiesListener.createListener(taskViewModel.get().getTask(), project);
      System.out.println(currentTask.getAction());
    }
  }

  //TODO structure the files into packages

  public void currentTaskCompleted() {

    currentTask = tutorial.getNextTask(currentTask);
    if (currentTask != null) { // perhaps refine and use a field like isLastTask and not a null check
      taskViewModel.set(new TaskViewModel(currentTask));
      currentTask.taskUpdated.addListener(
          this, TutorialViewModel::currentTaskCompleted);
      ActivitiesListener.createListener(currentTask, project);
    } else {
      //A dialog will summarize what data will be sent, it might be better to separate
      //the logic (gathering that info) into a separate ViewModel initialized here.
      System.out.println("Tutorial is done");
      //tutorial.setCompleted();
      //TODO gather the data here..
    }
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public Task getCurrentTask() {
    return this.currentTask;
  }
}
