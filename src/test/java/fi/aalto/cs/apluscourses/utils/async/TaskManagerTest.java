package fi.aalto.cs.apluscourses.utils.async;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TaskManagerTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testDefaultImplementationOfAllWithDelayTaskManager() {
    Runnable runnable1 = mock(Runnable.class);
    Runnable runnable2 = mock(Runnable.class);
    Runnable runnable3 = mock(Runnable.class);

    DelayTaskManager taskManager = new DelayTaskManager();
    List<Runnable> tasks = new ArrayList<>();
    tasks.add(taskManager.fork(runnable1));
    tasks.add(taskManager.fork(runnable2));
    tasks.add(taskManager.fork(runnable3));

    verifyNoInteractions(runnable1);
    verifyNoInteractions(runnable2);
    verifyNoInteractions(runnable3);

    Runnable unitedTask = taskManager.all(tasks);
    taskManager.join(unitedTask);

    verify(runnable1).run();
    verify(runnable2).run();
    verify(runnable3).run();

    verifyNoMoreInteractions(runnable1);
    verifyNoMoreInteractions(runnable2);
    verifyNoMoreInteractions(runnable3);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDefaultImplementationOfAllWithImmediateTaskManager() {
    Runnable runnable1 = mock(Runnable.class);
    Runnable runnable2 = mock(Runnable.class);
    Runnable runnable3 = mock(Runnable.class);

    ImmediateTaskManager taskManager = new ImmediateTaskManager();
    List<Void> tasks = new ArrayList<>();
    tasks.add(taskManager.fork(runnable1));
    tasks.add(taskManager.fork(runnable2));
    tasks.add(taskManager.fork(runnable3));

    verify(runnable1).run();
    verify(runnable2).run();
    verify(runnable3).run();

    Void unitedTask = taskManager.all(tasks);
    taskManager.join(unitedTask);

    verifyNoMoreInteractions(runnable1);
    verifyNoMoreInteractions(runnable2);
    verifyNoMoreInteractions(runnable3);
  }

  @Test
  public void testDefaultImplementationOfJoinAll() {
    DelayTaskManager taskManager = new DelayTaskManager();

    List<Runnable> taskList = new ArrayList<>();
    Runnable runnable1 = mock(Runnable.class);
    taskList.add(taskManager.fork(runnable1));
    Runnable runnable2 = mock(Runnable.class);
    taskList.add(taskManager.fork(runnable2));
    Runnable runnable3 = mock(Runnable.class);
    taskList.add(taskManager.fork(runnable3));

    taskManager.joinAll(taskList);

    verify(runnable1).run();
    verify(runnable2).run();
    verify(runnable3).run();

    verifyNoMoreInteractions(runnable1);
    verifyNoMoreInteractions(runnable2);
    verifyNoMoreInteractions(runnable3);
  }

  @Test
  public void testHelperTaskManagersJoinNull() {
    new DelayTaskManager().join(null);
    new ImmediateTaskManager().join(null);
    // nothing should happen, no exceptions should be thrown
  }
}
