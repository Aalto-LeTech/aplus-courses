package fi.aalto.cs.apluscourses.utils;

import java.util.concurrent.TimeUnit;

public class Stopwatch {

  private final long startTime;

  public Stopwatch() {
    startTime = System.nanoTime();
  }

  public long getTimeMs() {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
  }
}
