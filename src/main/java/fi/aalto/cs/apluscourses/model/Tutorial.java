package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.model.task.Task;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;


public class Tutorial {
  private final List<Task> tasks;

  private final Set<String> dependencies;

  @NotNull
  public final Event tutorialCompleted = new Event();

  public static final String TUTORIAL_SUBMIT_FILE_NAME = "_ideact_result";

  public Tutorial(Task[] tasks, String[] dependencies) {
    this.tasks = new ArrayList<>(Arrays.asList(tasks));
    this.dependencies = Arrays.stream(dependencies).collect(Collectors.toSet());
  }

  /**
   * Creates Tutorial from JSON.
   */
  public static Tutorial fromJsonObject(@NotNull JSONObject jsonObject) {
    return new Tutorial(JsonUtil.parseArray(jsonObject.getJSONArray("tasks"),
        JSONArray::getJSONObject, Task::fromJsonObject, Task[]::new),
        JsonUtil.parseArray(jsonObject.getJSONArray("moduleDependencies"),
            JSONArray::getString, String::new, String[]::new));
  }

  public List<Task> getTasks() {
    return tasks;
  }

  /**
   * Replaces the parameter task in the tasks list with a completed version of it, and returns the completed version.
   */
  public @Nullable Task setTaskAlreadyCompleted(@NotNull Task task) {
    int index = tasks.indexOf(task);
    if (index < 0) {
      return null;
    }
    var newTask = task.alreadyCompleted();
    tasks.set(index, newTask);
    return newTask;
  }

  /**
   * Method to get the next Task in row.
   *
   * @param task the current Task whose successor we are looking for.
   * @return the next Task to be performed
   */
  public @Nullable Task getNextTask(@NotNull Task task) {
    int index = tasks.indexOf(task);
    if (index != tasks.size() - 1) {
      return tasks.get(index + 1);
    }
    return null;
  }

  @NotNull
  public String getSubmissionPayload() {
    return "success";
  }

  public void onComplete() {
    this.tutorialCompleted.trigger();
  }

  public Set<String> getDependencies() {
    return dependencies;
  }
}
