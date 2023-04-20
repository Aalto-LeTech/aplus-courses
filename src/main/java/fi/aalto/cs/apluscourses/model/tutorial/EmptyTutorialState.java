package fi.aalto.cs.apluscourses.model.tutorial;

import org.jetbrains.annotations.Nullable;

public class EmptyTutorialState implements TutorialState {

  public static final TutorialState INSTANCE = new EmptyTutorialState();

  private EmptyTutorialState() {

  }

  @Override
  public @Nullable String getKey() {
    return null;
  }

  @Override
  public void activate() {
    // do nothing
  }

  @Override
  public void deactivate() {
    // do nothing
  }

  @Override
  public void goForward() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void goBackward() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canGoForward() {
    return false;
  }

  @Override
  public boolean canGoBackward() {
    return false;
  }
}
