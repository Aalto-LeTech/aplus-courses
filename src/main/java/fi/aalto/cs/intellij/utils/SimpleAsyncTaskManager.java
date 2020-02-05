package fi.aalto.cs.intellij.utils;

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
}
