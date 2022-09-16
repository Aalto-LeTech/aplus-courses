package fi.aalto.cs.apluscourses.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RestartableTaskTest {

  @Test
  void testRestart() throws InterruptedException {
    int max = 10;
    AtomicInteger num = new AtomicInteger(max);
    RestartableTask task = new RestartableTask(
        // Threads' sleeping times 9 sec, 8 sec, 7 sec, ..., 0 sec
        () -> Thread.sleep(num.getAndDecrement() * 1000L),
        null);
    Thread[] threads = new Thread[max];
    for (int i = 0; i < max; i++) {
      threads[i] = task.restart();
    }
    threads[max - 1].join();
    assertTrue(Arrays.stream(threads).noneMatch(Thread::isAlive));
  }
}
