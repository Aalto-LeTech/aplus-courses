package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.model.task.Task;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.JsonUtil;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;


public class Tutorial {
  private final List<Task> tasks;

  @NotNull
  public final Event tutorialCompleted = new Event();

  public Tutorial(Task[] tasks) {
    this.tasks = List.of(tasks);
  }

  public static Tutorial fromJsonObject(@NotNull JSONObject jsonObject) {
    return new Tutorial(JsonUtil.parseArray(jsonObject.getJSONArray("tasks"),
        JSONArray::getJSONObject, Task::fromJsonObject, Task[]::new));
  }

  public List<Task> getTasks() {
    return tasks;
  }

  /**
   * Method to get the next Task in row.
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
}
