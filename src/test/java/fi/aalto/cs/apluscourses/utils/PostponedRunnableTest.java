package fi.aalto.cs.apluscourses.utils;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PostponedRunnableTest {
  @Test
  void testUsesExecutorToRunRunnable() {
    AtomicInteger runnableCallCount = new AtomicInteger(0);
    AtomicInteger executorCallCount = new AtomicInteger(0);
    PostponedRunnable postponedRunnable = new PostponedRunnable(
        runnableCallCount::incrementAndGet,
        runnable -> {
          executorCallCount.incrementAndGet();
          runnable.run();
        }
    );

    postponedRunnable.run();

    Assertions.assertEquals(1, runnableCallCount.get(), "The given runnable is ran");
    Assertions.assertEquals(1, executorCallCount.get(), "The runnable is ran by the given executor");
  }
}
