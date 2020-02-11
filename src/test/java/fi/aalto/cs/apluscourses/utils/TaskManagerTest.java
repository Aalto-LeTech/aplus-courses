package fi.aalto.cs.apluscourses.utils;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class TaskManagerTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testDefaultImplementationWithDelayTaskManager() {
    TaskManager<Runnable> taskManager = new DelayTaskManager();

    Runnable runnable1 = mock(Runnable.class);
    Runnable runnable2 = mock(Runnable.class);
    Runnable runnable3 = mock(Runnable.class);

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
  public void testDefaultImplementationWithImmediateTaskManager() {
    TaskManager<Void> taskManager = new ImmediateTaskManager();

    Runnable runnable1 = mock(Runnable.class);
    Runnable runnable2 = mock(Runnable.class);
    Runnable runnable3 = mock(Runnable.class);

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
}
