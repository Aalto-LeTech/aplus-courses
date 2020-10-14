package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;

/**
 * A runnable whose {@code run} method uses some {@code invokeLater} method (by default {@code
 * ApplicationManager.getApplication()::invokeLater} with {@link ModalityState#NON_MODAL}is used).
 */
public class PostponedRunnable implements Runnable {
  @NotNull
  private Runnable task;
  @NotNull
  private Executor executor;

  /**
   * This constructor allows giving a custom {@code invokeLater} method, which is useful mostly for
   * testing purposes.
   */
  public PostponedRunnable(@NotNull Runnable task, @NotNull Executor executor) {
    this.task = task;
    this.executor = executor;
  }

  public PostponedRunnable(@NotNull Runnable task) {
    this(task, runnable ->
        ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL));
  }

  @Override
  public void run() {
    executor.execute(task);
  }
}
