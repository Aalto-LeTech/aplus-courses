package fi.aalto.cs.apluscourses.model.tutorial.switching;

public class SceneSwitchImpl extends SwitchBase<SceneSwitch> implements SceneSwitch {
  @Override
  public void goForward() {
    delegate(SceneSwitch::goForward);
  }

  @Override
  public void goBackward() {
    delegate(SceneSwitch::goBackward);
  }

  @Override
  public boolean canGoForward() {
    return delegate(SceneSwitch::canGoForward, false);
  }

  @Override
  public boolean canGoBackward() {
    return delegate(SceneSwitch::canGoBackward, false);
  }
}
