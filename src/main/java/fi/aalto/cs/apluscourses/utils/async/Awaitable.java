package fi.aalto.cs.apluscourses.utils.async;

@FunctionalInterface
public interface Awaitable {
  void await();
}
