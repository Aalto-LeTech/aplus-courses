package fi.aalto.cs.apluscourses.utils;

import java.util.TimerTask;
import org.jetbrains.annotations.NotNull;

public class DelegateTimerTask extends TimerTask {

  private final Runnable delegate;

  public DelegateTimerTask(@NotNull Runnable delegate) {
    this.delegate = delegate;
  }

  @Override
  public void run() {
    delegate.run();
  }
}
