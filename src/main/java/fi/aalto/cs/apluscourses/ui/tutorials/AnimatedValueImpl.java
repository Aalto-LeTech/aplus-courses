package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.jetbrains.annotations.NotNull;

public class AnimatedValueImpl implements AnimatedValue, ActionListener {
  private float value = 0.0f;
  private float from = 0.0f;
  private float to = 0.0f;
  private int duration = 0;
  private long end = 0;
  private int repeatMode = NO_REPEAT;
  private final @NotNull Timer timer;
  private final @NotNull Runnable callback;

  public AnimatedValueImpl(int delay, @NotNull Runnable callback) {
    timer = new Timer(delay, this);
    timer.setRepeats(false);
    this.callback = callback;
  }

  @Override
  public float get() {
    return value;
  }

  @Override
  public void set(float value) {
    this.value = value;
    to = value;
    from = value;
    repeatMode = NO_REPEAT;
    end = 0;
  }
  @Override
  public void animate(float from, float to, int duration, int repeatMode) {
    //timer.stop();
    if (from != this.from || to != this.to || duration != this.duration) {
      this.from = from;
      this.to = to;
      this.duration = duration;
      if (value < to && value < from || value > to && value > from) {
        value = from;
      }
      end = System.currentTimeMillis() + duration - (int) Math.abs(duration * (value - from) / (to - from));
    }
    this.repeatMode = repeatMode;
    if (!timer.isRunning() && (repeatMode != NO_REPEAT || value != to)) {
      timer.start();
    }
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    long current = System.currentTimeMillis();
    value = repeatMode == NO_REPEAT && current >= end
        ? to
        : from + getProgress(current) * (to - from);
    callback.run();
  }

  private float getProgress(long time) {
    switch (repeatMode) {
      case BACK_AND_FORTH:
        return 1f - Math.abs(((1f + (time - end) / (float) duration) % 2f + 2f) % 2f - 1f);
      default:
        return (1f + (time - end) / (float) duration) % 1f;
    }
  }
}
