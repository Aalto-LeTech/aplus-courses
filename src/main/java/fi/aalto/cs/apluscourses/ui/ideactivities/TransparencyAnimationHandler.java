package fi.aalto.cs.apluscourses.ui.ideactivities;

public class TransparencyAnimationHandler {

  private final TransparentComponent component;

  private long lastTransitionUpdate;

  private static final float MIN_TRANSPARENCY = 0.55f;
  private static final float MAX_TRANSPARENCY = 1.0f;
  private static final float TRANSITION_STEP = 0.0015f;

  public TransparencyAnimationHandler(TransparentComponent component) {
    this.component = component;
  }

  public boolean isInAnimation() {
    var coefficient = component.getTransparencyCoefficient();
    return !(coefficient == MIN_TRANSPARENCY || coefficient == MAX_TRANSPARENCY);
  }

  public void resetAnimationProgress() {
    lastTransitionUpdate = 0;
  }

  /**
   * Updates the status of transparency fading animation.
   *
   * @param turningOpaque If true, the component is supposed to be turning opaque.
   *                      Otherwise, the component is becoming transparent.
   */
  public void update(boolean turningOpaque) {
    if (lastTransitionUpdate == 0) {
      lastTransitionUpdate = System.currentTimeMillis();
      return;
    }

    final float transitionStep = turningOpaque ? TRANSITION_STEP : -TRANSITION_STEP;
    final long timeSinceLastUpdate = System.currentTimeMillis() - lastTransitionUpdate;

    float currentCoefficient = component.getTransparencyCoefficient();
    currentCoefficient += transitionStep * timeSinceLastUpdate;

    if (currentCoefficient < MIN_TRANSPARENCY) {
      currentCoefficient = MIN_TRANSPARENCY;
      resetAnimationProgress();
    }

    if (currentCoefficient > MAX_TRANSPARENCY) {
      currentCoefficient = MAX_TRANSPARENCY;
      resetAnimationProgress();
    }

    component.setTransparencyCoefficient(currentCoefficient);
    lastTransitionUpdate = System.currentTimeMillis();
  }
}
