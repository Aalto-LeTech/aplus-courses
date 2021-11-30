package fi.aalto.cs.apluscourses.model.task;

public interface CancelHandler {
  void onCancel();

  void onForceCancel();
}
