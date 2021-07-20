package fi.aalto.cs.apluscourses.model;

public interface LazyLoader {
  void addLazyLoaded(long id);

  boolean isLazyLoaded(long id);
}
