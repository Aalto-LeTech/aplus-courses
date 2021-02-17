package fi.aalto.cs.apluscourses.utils.async;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class SimpleAsyncTaskManagerTest {

  @Test
  public void testFork() {
    SimpleAsyncTaskManager taskManager = new SimpleAsyncTaskManager();

    Runnable runnable = mock(Runnable.class);

    Thread task = taskManager.fork(runnable);
    taskManager.join(task);

    verify(runnable).run();

    verifyNoMoreInteractions(runnable);
  }

  @Test
  public void testJoinNull() {
    new SimpleAsyncTaskManager().join(null);
    // nothing should happen, no exceptions should be thrown
  }

  @Test
  public void testAll() {
    SimpleAsyncTaskManager taskManager = new SimpleAsyncTaskManager();

    Runnable runnable1 = mock(Runnable.class);
    Runnable runnable2 = mock(Runnable.class);
    Runnable runnable3 = mock(Runnable.class);

    List<Thread> tasks = new ArrayList<>();
    tasks.add(taskManager.fork(runnable1));
    tasks.add(taskManager.fork(runnable2));
    tasks.add(taskManager.fork(runnable3));

    Thread unitedTask = taskManager.all(tasks);
    taskManager.join(unitedTask);

    verify(runnable1).run();
    verify(runnable2).run();
    verify(runnable3).run();

    verifyNoMoreInteractions(runnable1);
    verifyNoMoreInteractions(runnable2);
    verifyNoMoreInteractions(runnable3);
  }
}
