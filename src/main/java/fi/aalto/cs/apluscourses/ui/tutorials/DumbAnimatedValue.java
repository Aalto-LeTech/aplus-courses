package fi.aalto.cs.apluscourses.ui.tutorials;

public class DumbAnimatedValue implements AnimatedValue {

  @Override
  public float get() {
    return 0f;
  }

  @Override
  public void set(float value) {
    // do nothing
  }

  @Override
  public void animate(float high, float low, int duration, int repeatMode) {
    // do nothing
  }
}
