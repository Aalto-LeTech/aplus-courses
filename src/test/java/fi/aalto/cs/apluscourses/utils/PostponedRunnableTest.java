package fi.aalto.cs.apluscourses.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class PostponedRunnableTest {
  @Test
  public void testUsesExecutorToRunRunnable() {
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

    Assert.assertEquals("The given runnable is ran", 1, runnableCallCount.get());
    Assert.assertEquals("The runnable is ran by the given executor", 1, executorCallCount.get());
  }
}
