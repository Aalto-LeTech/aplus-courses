package fi.aalto.cs.apluscourses.ui.tutorials;

public interface AnimatedValue {
  int NO_REPEAT = 0;
  int BACK_AND_FORTH = 1;
  int START_OVER = 2;

  float get();

  void set(float value);

  void animate(float from, float to, int duration, int repeatMode);

  default void dim() {
    animate(1.0f, 0.67f, 100, NO_REPEAT);
  }

  default void fadeOut() {
    animate(1.0f, 0.0f, 300, NO_REPEAT);
  }

  default void fadeIn() {
    animate(0.0f, 1.0f, 300, NO_REPEAT);
  }
}
