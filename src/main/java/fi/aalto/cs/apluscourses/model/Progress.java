package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

public class Progress {
  private int value = 0;
  private int maxValue;
  private final String label;
  private final boolean indeterminate;

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

  public void increment() {
    this.value++;
  }

  public void incrementMaxValue(int amount) {
    this.maxValue = this.maxValue + amount;
  }

}
