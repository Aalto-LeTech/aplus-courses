package fi.aalto.cs.apluscourses.model.task;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

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
import org.json.JSONException;
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
  private final boolean isAlreadyCompleted;

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
              boolean isFreeRange,
              boolean isAlreadyCompleted) {
    this.instruction = instruction;
    this.info = info;
    this.assertClosed = assertClosed;
    this.components = components;
    this.componentArguments = componentArguments;
    this.action = action;
    this.actionArguments = actionArguments;
    this.isFreeRange = isFreeRange;
    this.isAlreadyCompleted = isAlreadyCompleted;
  }

  /**
   * Creates a copy of the current task that is already completed and free-range.
   */
  public Task alreadyCompleted() {
    return new Task(
        getText("ui.tutorial.Task.done") + " " + instruction,
        getText("ui.tutorial.Task.alreadyCompleted") + info,
        assertClosed,
        components,
        componentArguments,
        action,
        actionArguments,
        true,
        true
    );
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

      var reaction = new Reaction[0];
      if (isAlreadyCompleted) {
        reaction = new Reaction[] {new AlreadyCompleteReaction()};
      } else if (isFreeRange) {
        reaction = new Reaction[] {new ImDoneReaction()};
      }

      var presenter = activityFactory.createPresenter(componentName, attachPopup ? instruction : null,
          attachPopup ? info : null, componentArguments, actionArguments, assertClosed, reaction, isAlreadyCompleted);
      if (isAlreadyCompleted) {
        presenter.setAlreadyCompleted();
      }
      presenters.add(presenter);
    }

    listener = activityFactory.createListener(action, actionArguments, this);
    if (isAlreadyCompleted) {
      listener.setAlreadyCompleted();
    }
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
    } else if (componentJsonEntry instanceof String) {
      componentArray = new String[] {(String) componentJsonEntry};
    } else {
      throw new JSONException("The field \"component\" is of an invalid type");
    }

    return new Task(
        jsonObject.getString("instruction"),
        jsonObject.getString("info"),
        parseAssert(jsonObject.optJSONArray("assertClosed")),
        componentArray,
        parseArguments(jsonObject.optJSONObject("componentArguments")),
        jsonObject.getString("action"),
        parseArguments(jsonObject.optJSONObject("actionArguments")),
        jsonObject.optBoolean("freeRange", false),
        false);
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

  @Override
  public void onForceCancel() {
    notifyObservers(Observer::onForceCancelled);
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

  public boolean isAlreadyCompleted() {
    return isAlreadyCompleted;
  }

  public interface Observer {
    void onCancelled();

    void onForceCancelled();

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

  private class AlreadyCompleteReaction implements Reaction {

    @Override
    public String getLabel() {
      return "Next task";
    }

    @Override
    public void react() {
      onHappened(false);
    }
  }
}

