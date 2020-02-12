package fi.aalto.cs.apluscourses.utils.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public class SimpleAsyncTaskManager implements TaskManager<CompletableFuture<Void>> {

  @Override
  public CompletableFuture<Void> fork(Runnable runnable) {
    return CompletableFuture.runAsync(runnable);
  }

  @Override
  public void join(@Nullable CompletableFuture<Void> task) {
    if (task != null) {
      task.join();
    }
  }

  @Override
  public CompletableFuture<Void> all(List<CompletableFuture<Void>> tasks) {
    return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
  }
}
