package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;

public interface ActivitiesListener {

  void registerListener(Project project);

  void unregisterListener();

}