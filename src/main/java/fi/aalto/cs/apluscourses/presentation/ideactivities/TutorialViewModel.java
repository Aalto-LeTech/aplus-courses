package fi.aalto.cs.apluscourses.presentation.ideactivities;

import fi.aalto.cs.apluscourses.intellij.notifications.TaskNotifier;
import fi.aalto.cs.apluscourses.model.Tutorial;
import fi.aalto.cs.apluscourses.model.TutorialExercise;
import fi.aalto.cs.apluscourses.model.task.ActivityFactory;
import fi.aalto.cs.apluscourses.model.task.Task;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TutorialViewModel implements Task.Observer {

  @NotNull
  private final TutorialExercise tutorialExercise;

  @NotNull
  private final TutorialDialogs dialogs;

  @NotNull
  private final ActivityFactory activityFactory;

  @NotNull
  private final TaskNotifier taskNotifier;

  private final Object lock = new Object();

  private Task currentTask = null;

  private boolean isCompleted;

  private int currentTaskIndex;

  private int unlockedIndex;

  private final int tasksAmount;

  /**
   * Constructor.
   */
  public TutorialViewModel(@NotNull TutorialExercise tutorialExercise,
                           @NotNull ActivityFactory activityFactory,
                           @NotNull TaskNotifier taskNotifier,
                           @NotNull TutorialDialogs dialogs) {
    this.tutorialExercise = tutorialExercise;
    this.dialogs = dialogs;
    List<Task> tasks = tutorialExercise.getTutorial().getTasks();
    if (!tasks.isEmpty()) {
      this.currentTask = tasks.get(0);
    }
    isCompleted = false;
    currentTaskIndex = 1;
    unlockedIndex = 1;
    tasksAmount = tasks.size();
    this.activityFactory = activityFactory;
    this.taskNotifier = taskNotifier;
  }

  /**
   * Begins the nextTask which is indicated by the currentTask variable.
   */
  public void startCurrentTask() {
    synchronized (lock) {
      currentTask.addObserver(this);
      currentTask.startTask(activityFactory);
    }
  }

  /**
   * Ends the current task.
   */
  public void endCurrentTask() {
    synchronized (lock) {
      currentTask.removeObserver(this);
      currentTask.endTask();
    }
  }

  @Override
  public void onCancelled() {
    confirmCancel();
  }

  @Override
  public void onForceCancelled() {
    cancelTutorial(); // does not ask for confirmation
  }

  @Override
  public void onAutoCompleted() {
    if (!currentTask.isAlreadyCompleted()) {
      endCurrentTask();
      currentTask = getTutorial().setTaskAlreadyCompleted(currentTask);
      startCurrentTask();
    }
  }

  @Override
  public void onCompleted() {
    taskNotifier.notifyEndTask(tutorialExercise.getTutorial().getTasks().indexOf(currentTask));
    currentTaskCompleted();
  }

  /**
   * Sets the currentTask as completed and frees up any resources associated with it.
   * If this task was the last one the Tutorial is completed,
   * if not, then the currentTask is set to point to the next Task to be done.
   */
  public void currentTaskCompleted() {
    synchronized (lock) {
      endCurrentTask();
      Tutorial tutorial = tutorialExercise.getTutorial();
      currentTask = tutorial.getNextTask(currentTask);
      if (currentTask == null) {
        isCompleted = true;
        tutorial.onComplete();
      } else {
        incrementTaskIndex();
        startCurrentTask();
      }
    }
  }

  /**
   * Sets the currentTask as completed and frees up any resources associated with it.
   * CurrentTask is set to the task with the given index.
   */
  public void changeTask(int newTaskIndex) {
    synchronized (lock) {
      endCurrentTask();
      Tutorial tutorial = tutorialExercise.getTutorial();
      currentTask = tutorial.getTasks().get(newTaskIndex);
      this.currentTaskIndex = newTaskIndex;
      startCurrentTask();
    }
  }

  /**
   * Functionality for canceling the Tutorial, mainly frees up resources.
   */
  public void cancelTutorial() {
    synchronized (lock) {
      if (currentTask != null) {
        endCurrentTask();
        currentTask = null;
        tutorialExercise.getTutorial().onComplete();
      }
    }
  }

  public @NotNull TutorialExercise getExercise() {
    return tutorialExercise;
  }

  public @NotNull String getTitle() {
    return APlusLocalizationUtil.getEnglishName(tutorialExercise.getName());
  }

  public @NotNull Tutorial getTutorial() {
    return tutorialExercise.getTutorial();
  }

  public int getCurrentTaskIndex() {
    return currentTaskIndex;
  }

  private void incrementTaskIndex() {
    currentTaskIndex++;
    unlockedIndex = Integer.max(currentTaskIndex, unlockedIndex);
  }

  public int getTasksAmount() {
    return tasksAmount;
  }

  public boolean isCompleted() {
    return isCompleted;
  }

  /**
   * Cancels the tutorial if the user confirms it.
   */
  public void confirmCancel() {
    if (dialogs.confirmCancel(this)) {
      cancelTutorial();
    }
  }

  public int getUnlockedIndex() {
    return unlockedIndex;
  }
}
