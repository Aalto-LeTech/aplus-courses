package fi.aalto.cs.apluscourses.intellij.utils;

import fi.aalto.cs.apluscourses.model.Task;

public class ActivitiesListenerFactory {

  private ActivitiesListenerFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static ActivitiesListener createListener(Task task) {
    switch (task.getAction()) {
      case "editor.open":
        return new OpenFileListener(task);
      case "assignment_tree.submit":
        //Listener for submitting assignments?
      default:
        return null;
    }
  }
}
