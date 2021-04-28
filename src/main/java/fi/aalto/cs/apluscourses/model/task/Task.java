package fi.aalto.cs.apluscourses.model.task;

import fi.aalto.cs.apluscourses.utils.Event;
import org.jetbrains.annotations.NotNull;

public class Task {
  public final @NotNull Event taskCompleted = new Event();

  private final @NotNull String instruction;
  private final @NotNull String info;
  private final @NotNull String component;
  private final @NotNull Arguments componentArguments;
  private final @NotNull String action;
  private final @NotNull Arguments actionArguments;

  private ActivitiesListener listener;
  private ComponentPresenter presenter;

  /**
   * Task constructor.
   */
  public Task(@NotNull String instruction,
              @NotNull String info,
              @NotNull String component,
              @NotNull Arguments componentArguments,
              @NotNull String action,
              @NotNull Arguments actionArguments) {
    this.instruction = instruction;
    this.action = action;
    this.actionArguments = actionArguments;
    this.info = info;
    this.component = component;
    this.componentArguments = componentArguments;
  }

  public @NotNull String getAction() {
    return action;
  }

  public synchronized void endTask() {
    if (listener != null) {
      listener.unregisterListener();
      listener = null;
    }
    if (presenter != null) {
      presenter.removeHighlight();
      presenter = null;
    }
  }

  public synchronized boolean startTask(ActivityFactory activityFactory) {
    if (presenter != null || listener != null) {
      throw new IllegalStateException();
    }
    presenter = activityFactory.createPresenter(component, instruction, info, componentArguments);
    listener = activityFactory.createListener(action, actionArguments, taskCompleted::trigger);
    if (listener.registerListener()) {
      return true;
    }
    presenter.highlight();
    return false;
  }
}

