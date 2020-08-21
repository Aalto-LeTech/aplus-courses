package fi.aalto.cs.apluscourses.utils.async;

import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract interface for asynchronous execution following the fork-join model of task
 * parallelism.
 *
 * <p>Note that it is not, however, required that the implementing classes actually use any
 * concurrent computation.</p>
 *
 * @param <T> Type of the task objects (or futures) that are used as identifiers for asynchronous
 *            tasks
 */
public interface TaskManager<T> {
  /**
   * Runs a given {@link Runnable} as a new task and returns an identifier object for that task.
   *
   * @param runnable Subprogram to be run as a new task.
   * @return A task identifier whose type is implementation-specific.
   */
  T fork(Runnable runnable);

  /**
   * Joins the given task.
   *
   * <p>Basically, it means that {@code run()} method of the {@link Runnable} object given to
   * {@code fork()} returns before (in the sense of happens-before relationship of Java's memory
   * model) this method returns (when called with the task identifier returned by the said call to
   * {@code fork}).</p>
   *
   * <p>If {@code task} is null, this method is expected just return.</p>
   *
   * @param task A task identifier returned by an antecedent call to {@code fork}, or null.
   */
  void join(@Nullable T task);

  /**
   * Merge multiple tasks into one.
   *
   * @param tasks {@link List} of task identifiers.
   * @return A task identifier that, when passed to {@code join}, ensures that all the tasks
   *         given to this function, are joined.
   */
  default T all(List<T> tasks) {
    return fork(() -> {
      for (T task : tasks) {
        join(task);
      }
    });
  }

  default void joinAll(List<T> tasks) {
    join(all(tasks));
  }
}
