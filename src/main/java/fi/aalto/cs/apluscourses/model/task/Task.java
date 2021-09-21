package fi.aalto.cs.apluscourses.model.task;

import fi.aalto.cs.apluscourses.utils.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
  private final @NotNull String[] components;
  private final @NotNull Arguments componentArguments;
  private final @NotNull String action;
  private final @NotNull Arguments actionArguments;
  private final boolean isFreeRange;

  private ActivitiesListener listener;
  private final List<ComponentPresenter> presenters = new ArrayList<>();

  private final Set<@NotNull Observer> observers = Collections.synchronizedSet(new HashSet<>());

  /**
   * Task constructor.
   */
  public Task(@NotNull String instruction,
              @NotNull String info,
              String @NotNull [] assertClosed,
              @NotNull String[] components,
              @NotNull Arguments componentArguments,
              @NotNull String action,
              @NotNull Arguments actionArguments,
              boolean isFreeRange) {
    this.instruction = instruction;
    this.assertClosed = assertClosed;
    this.action = action;
    this.actionArguments = actionArguments;
    this.info = info;
    this.components = components;
    this.componentArguments = componentArguments;
    this.isFreeRange = isFreeRange;
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

    for (var presenter : presenters) {
      if (presenter != null) {
        presenter.removeHighlight();
      }
    }

    presenters.clear();
  }

  /**
   * Starts a task.
   *
   * @param activityFactory Creator for listener and presenter objects.
   */
  public synchronized void startTask(ActivityFactory activityFactory) {
    if (!presenters.isEmpty() || listener != null) {
      throw new IllegalStateException();
    }

    for (int i = 0; i < components.length; ++i) {
      var componentName = components[i];
      boolean attachPopup = i == 0; // only the first component in the array has the popup attached

      presenters.add(activityFactory.createPresenter(componentName, attachPopup ? instruction : null,
          attachPopup ? info : null, componentArguments, actionArguments, assertClosed,
          isFreeRange ? new Reaction[] { new ImDoneReaction() } : new Reaction[0]));
    }

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
    String[] componentArray;
    Object componentJsonEntry = jsonObject.get("component");

    // entry "component" can be either String or String[] - the former gets converted
    // to a String[] with only one element - this is done to preserve backwards compatibility
    if (componentJsonEntry instanceof JSONArray) {
      componentArray = JsonUtil.parseArray(jsonObject.getJSONArray("component"),
          JSONArray::getString, Function.identity(), String[]::new);
    } else {
      componentArray = new String[] { (String) componentJsonEntry };
    }

    return new Task(
        jsonObject.getString("instruction"),
        jsonObject.getString("info"),
        parseAssert(jsonObject.optJSONArray("assertClosed")),
        componentArray,
        parseArguments(jsonObject.optJSONObject("componentArguments")),
        jsonObject.getString("action"),
        parseArguments(jsonObject.optJSONObject("actionArguments")),
        jsonObject.optBoolean("freeRange", false));
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
  public synchronized void onStarted() {
    for (var presenter : presenters) {
      presenter.setCancelHandler(this);
      presenter.highlight();
    }
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

  private class ImDoneReaction implements Reaction {

    @Override
    public String getLabel() {
      return "I'm done!";
    }

    @Override
    public void react() {
      onHappened(false);
    }
  }
}

