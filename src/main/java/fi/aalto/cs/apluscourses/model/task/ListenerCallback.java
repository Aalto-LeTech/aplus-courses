package fi.aalto.cs.apluscourses.model.task;

public interface ListenerCallback {
  void onHappened(boolean isInitial);
  void onStarted();
}
