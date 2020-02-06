package fi.aalto.cs.intellij.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SimpleAsyncTaskManager implements TaskManager<CompletableFuture<Void>> {

  @Override
  public CompletableFuture<Void> fork(Runnable runnable) {
    return CompletableFuture.runAsync(runnable);
  }

  @Override
  public void join(CompletableFuture<Void> task) {
    task.join();
  }

  @Override
  public CompletableFuture<Void> all(List<CompletableFuture<Void>> tasks) {
    return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
  }
}
