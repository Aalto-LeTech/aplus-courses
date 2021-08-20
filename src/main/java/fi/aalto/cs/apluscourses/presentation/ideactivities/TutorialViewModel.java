package fi.aalto.cs.apluscourses.presentation.ideactivities;

import fi.aalto.cs.apluscourses.intellij.notifications.TaskNotifier;
import fi.aalto.cs.apluscourses.model.Tutorial;
import fi.aalto.cs.apluscourses.model.TutorialExercise;
import fi.aalto.cs.apluscourses.model.task.ActivityFactory;
import fi.aalto.cs.apluscourses.model.task.Task;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TutorialViewModel {

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
    currentTaskIndex = 0;
    unlockedIndex = 0;
    tasksAmount = tasks.size();
    this.activityFactory = activityFactory;
    this.taskNotifier = taskNotifier;
  }

  /**
   * Begins the nextTask which is indicated by the currentTask variable.
   */
  public void startNextTask() {
    synchronized (lock) {
      currentTask.taskCompleted.addListener(this, TutorialViewModel::currentTaskCompleted);
      currentTask.taskCanceled.addListener(this, TutorialViewModel::confirmCancel);
      incrementTaskIndex();
      if (currentTask.startTask(activityFactory)) {
        taskNotifier.notifyAlreadyEndTask(tutorialExercise.getTutorial().getTasks().indexOf(currentTask),
            currentTask.getInstruction());
        currentTask.setAlreadyComplete(true);
        currentTaskCompleted();
      }
      // The Task/Tutorial has been completed prematurely
      // because the Activity was already performed.
      // E.g. the file was open already, variable renamed etc.
      // No need to show the instructions for this Task
      // as we are proceeding directly to the next one.
      // We can instead inform the user that they have already completed it
    }
  }

  /**
   * Sets the currentTask as completed and frees up any resources associated with it.
   * If this task was the last one the Tutorial is completed,
   * if not, then the currentTask is set to point to the next Task to be done.
   */
  public void currentTaskCompleted() {
    synchronized (lock) {
      if (!currentTask.getAlreadyComplete()) {
        taskNotifier.notifyEndTask(tutorialExercise.getTutorial().getTasks().indexOf(currentTask));
      }
      currentTask.endTask();
      currentTask.taskCompleted.removeCallback(this);
      currentTask.taskCanceled.removeCallback(this);
      Tutorial tutorial = tutorialExercise.getTutorial();
      currentTask = tutorial.getNextTask(currentTask);
      if (currentTask == null) {
        isCompleted = true;
        tutorial.onComplete();
      } else {
        startNextTask();
      }
    }
  }

  /**
   * Sets the currentTask as completed and frees up any resources associated with it.
   * CurrentTask is set to the task with the given index.
   */
  public void changeTask(int newTaskIndex) {
    synchronized (lock) {
      Tutorial tutorial = tutorialExercise.getTutorial();
      currentTask.endTask();
      currentTask.taskCompleted.removeCallback(this);
      currentTask = tutorial.getTasks().get(newTaskIndex);
      this.currentTaskIndex = newTaskIndex;
      if (currentTask == null) {
        tutorial.onComplete();
      } else {
        startNextTask();
      }
    }
  }

  /**
   * Functionality for canceling the Tutorial, mainly frees up resources.
   */
  public void cancelTutorial() {
    synchronized (lock) {
      if (currentTask != null) {
        currentTask.endTask();
        currentTask.taskCompleted.removeCallback(this);
        currentTask.taskCanceled.removeCallback(this);
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
