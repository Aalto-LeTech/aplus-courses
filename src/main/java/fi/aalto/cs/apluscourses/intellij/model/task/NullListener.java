package fi.aalto.cs.apluscourses.intellij.model.task;

import fi.aalto.cs.apluscourses.model.task.ListenerCallback;

public class NullListener extends ActivitiesListenerBase<Boolean> {
  protected NullListener(ListenerCallback callback) {
    super(callback);
  }

  public static NullListener create(ListenerCallback callback) {
    return new NullListener(callback);
  }

  @Override
  protected boolean checkOverride(Boolean param) {
    return false;
  }

  @Override
  protected void registerListenerOverride() {

  }

  @Override
  protected void unregisterListenerOverride() {

  }
}
