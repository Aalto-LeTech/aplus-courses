package fi.aalto.cs.apluscourses.model;

public interface ExercisesLazyLoader {
  void setLazyLoadedGroup(long id);

  boolean isLazyLoadedGroup(long id);
}
