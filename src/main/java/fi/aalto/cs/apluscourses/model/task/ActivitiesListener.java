package fi.aalto.cs.apluscourses.model.task;

public interface ActivitiesListener {

  void registerListener();

  void unregisterListener();

  void setAlreadyCompleted();
}
