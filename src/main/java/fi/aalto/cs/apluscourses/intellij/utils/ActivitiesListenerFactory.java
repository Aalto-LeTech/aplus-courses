package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Task;

public class ActivitiesListenerFactory {

    public static void createListener(Task task, Project project) {
      switch (task.getAction()) {
      case "editor.open":
        new OpenFileListener(task, project).registerListeners(project);
        //openListener.registerListeners(project); //called in the TaskViewModel
      case "assignment_tree.submit":
         //Listener for submitting assignments?
      default:
          
      }

    }

}
