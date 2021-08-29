package fi.aalto.cs.apluscourses.model.task;

import fi.aalto.cs.apluscourses.utils.JsonUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class Task implements CancelHandler, ListenerCallback {
  private final @NotNull String instruction;
  private final @NotNull String info;
  private final String @NotNull [] assertClosed;
  private final @NotNull String component;
  private final @NotNull Arguments componentArguments;
  private final @NotNull String action;
  private final @NotNull Arguments actionArguments;

  private ActivitiesListener listener;
  private ComponentPresenter presenter;

  private final Set<@NotNull Observer> observers = Collections.synchronizedSet(new HashSet<>());

  /**
   * Task constructor.
   */
  public Task(@NotNull String instruction,
              @NotNull String info,
              String @NotNull [] assertClosed,
              @NotNull String component,
              @NotNull Arguments componentArguments,
              @NotNull String action,
              @NotNull Arguments actionArguments) {
    this.instruction = instruction;
    this.assertClosed = assertClosed;
    this.action = action;
    this.actionArguments = actionArguments;
    this.info = info;
    this.component = component;
    this.componentArguments = componentArguments;
  }

  public @NotNull String getAction() {
    return action;
  }

  /**
   * Ends the task.
   */
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

  /**
   * Starts a task.
   *
   * @param activityFactory Creator for listener and presenter objects.
   */
  public synchronized void startTask(ActivityFactory activityFactory) {
    if (presenter != null || listener != null) {
      throw new IllegalStateException();
    }
    presenter = activityFactory.createPresenter(component, instruction, info, componentArguments,
        actionArguments, assertClosed);
    presenter.setCancelHandler(this);
    listener = activityFactory.createListener(action, actionArguments, this);
    listener.registerListener();
  }

  /**
   * Builds a Task object from JSON.
   *
   * @param jsonObject JSON.
   * @return A task.
   */
  public static @NotNull Task fromJsonObject(@NotNull JSONObject jsonObject) {
    return new Task(
        jsonObject.getString("instruction"),
        jsonObject.getString("info"),
        parseAssert(jsonObject.optJSONArray("assertClosed")),
        jsonObject.getString("component"),
        parseArguments(jsonObject.optJSONObject("componentArguments")),
        jsonObject.getString("action"),
        parseArguments(jsonObject.optJSONObject("actionArguments"))
    );
  }

  protected static @NotNull Arguments parseArguments(@Nullable JSONObject jsonObject) {
    return jsonObject == null ? Arguments.empty()
        : JsonUtil.parseObject(jsonObject, JSONObject::get,
            Function.identity(), Function.identity())::get;
  }

  @NotNull
  public String getInstruction() {
    return instruction;
  }

  @Override
  public void onCancel() {
    notifyObservers(Observer::onCancelled);
  }

  protected static String @NotNull [] parseAssert(@Nullable JSONArray jsonObject) {
    return jsonObject == null ? new String[0]
        : JsonUtil.parseArray(jsonObject, JSONArray::getString,
            Function.identity(), String[]::new);
  }

  @Override
  public void onHappened(boolean isInitial) {
    notifyObservers(isInitial ? Observer::onAutoCompleted : Observer::onCompleted);
  }

  @Override
  public void onStarted() {
    presenter.highlight();
  }

  private void notifyObservers(@NotNull Consumer<@NotNull Observer> method) {
    for (var observer : observers.toArray(Observer[]::new)) {
      method.accept(observer);
    }
  }

  public void addObserver(@NotNull Observer observer) {
    observers.add(observer);
  }

  public void removeObserver(@NotNull Observer observer) {
    observers.remove(observer);
  }

  public interface Observer {
    void onCancelled();

    void onAutoCompleted();

    void onCompleted();
  }
}

