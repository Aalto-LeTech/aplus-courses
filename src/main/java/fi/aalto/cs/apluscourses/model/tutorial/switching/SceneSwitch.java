package fi.aalto.cs.apluscourses.model.tutorial.switching;

public interface SceneSwitch extends Switch<SceneSwitch> {
  void goForward();

  void goBackward();

  boolean canGoForward();

  boolean canGoBackward();
}
