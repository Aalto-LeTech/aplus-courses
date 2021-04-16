package fi.aalto.cs.apluscourses.intellij.utils;

import fi.aalto.cs.apluscourses.model.Task;

public class ActivitiesListenerFactory {

  private ActivitiesListenerFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static ActivitiesListener createListener(Task task) {
    switch (task.getAction()) {
      case "editorOpen":
        return new OpenFileListener(task);
      case "assignmentTreeSubmit":
        //Listener for submitting assignments?
      default:
        return null;
    }
  }
}
