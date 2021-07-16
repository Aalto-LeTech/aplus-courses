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

  private final TutorialExercise tutorialExercise;
  private final ActivityFactory activityFactory;
  private final TaskNotifier taskNotifier;
  private final Object lock = new Object();

  private Task currentTask = null;

  /**
   * Constructor.
   */
  public TutorialViewModel(@NotNull TutorialExercise tutorialExercise,
                           @NotNull ActivityFactory activityFactory,
                           @NotNull TaskNotifier taskNotifier) {
    this.tutorialExercise = tutorialExercise;
    List<Task> tasks = tutorialExercise.getTutorial().getTasks();
    if (!tasks.isEmpty()) {
      this.currentTask = tasks.get(0);
    }
    this.activityFactory = activityFactory;
    this.taskNotifier = taskNotifier;
  }

  /**
   * Begins the nextTask which is indicated by the currentTask variable.
   */
  public void startNextTask() {
    synchronized (lock) {
      currentTask.taskCompleted.addListener(this, TutorialViewModel::currentTaskCompleted);
      if (currentTask.startTask(activityFactory)) {
        taskNotifier.notifyAlreadyEndTask(tutorialExercise.getTutorial().getTasks().indexOf(currentTask));
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
   * Sets the currentTask as completed and fress up any resources associated with it.
   * If this task was the last one the Tutorial is completed,
   * if not, then the currentTask is set to point to the next Task to be done.
   */
  public void currentTaskCompleted() {
    synchronized (lock) {
      if (!currentTask.getAlreadyComplete()) {
        taskNotifier.notifyEndTask(tutorialExercise.getTutorial().getTasks().indexOf(currentTask));
      }

      Tutorial tutorial = tutorialExercise.getTutorial();
      currentTask.endTask();
      currentTask.taskCompleted.removeCallback(this);
      currentTask = tutorial.getNextTask(currentTask);
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
        currentTask = null;
        tutorialExercise.getTutorial().onComplete();
      }
    }
  }


  public @NotNull String getTitle() {
    return APlusLocalizationUtil.getEnglishName(tutorialExercise.getName());
  }

  public @NotNull Tutorial getTutorial() {
    return tutorialExercise.getTutorial();
  }
}
