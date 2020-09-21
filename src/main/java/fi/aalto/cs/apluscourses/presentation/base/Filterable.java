package fi.aalto.cs.apluscourses.presentation.base;

public interface Filterable {
  void addVisibilityListener(Listener listener);

  interface Listener {
    void visibilityChanged(boolean isVisible);
  }
}
