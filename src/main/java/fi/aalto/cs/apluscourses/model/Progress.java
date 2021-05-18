package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Event;
import org.jetbrains.annotations.NotNull;

public class Progress {
  private int value = 0;
  private final int maxValue;
  private final String label;
  private final boolean indeterminate;
  public final Event updated = new Event();

  /**
   * Constructor for Progress.
   */
  public Progress(int maxValue, @NotNull String label, boolean indeterminate) {
    this.maxValue = maxValue;
    this.label = label;
    this.indeterminate = indeterminate;
  }

  public int getValue() {
    return this.value;
  }

  public int getMaxValue() {
    return this.maxValue;
  }

  public String getLabel() {
    return this.label;
  }

  public boolean getIndeterminate() {
    return this.indeterminate;
  }

  public boolean isFinished() {
    return this.value >= this.maxValue;
  }

  public void increment() {
    this.value++;
    updated.trigger();
  }

  public void finish() {
    this.value = this.maxValue;
    updated.trigger();
  }

}
