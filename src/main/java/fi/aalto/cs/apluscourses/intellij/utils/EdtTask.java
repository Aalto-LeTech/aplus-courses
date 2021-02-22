package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.application.ApplicationManager;

/**
 * A task that is executed in the event dispatcher thread.
 *
 * @param <T> Type of the result.
 */
public abstract class EdtTask<T> {
  private volatile T result; //  NOSONAR

  public T executeAndWait() {
    ApplicationManager.getApplication().invokeAndWait(this::executeInternal);
    return result;
  }

  private void executeInternal() {
    result = execute();
  }

  protected abstract T execute();
}
