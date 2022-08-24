package fi.aalto.cs.apluscourses.utils;

@FunctionalInterface
public interface InterruptibleTask {
  void run() throws InterruptedException;
}
