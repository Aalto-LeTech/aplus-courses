package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.Optional;

public class ProgressViewModel {
  public final ObservableProperty<Integer> maxValue = new ObservableReadWriteProperty<>(0);
  public final ObservableProperty<Integer> value = new ObservableReadWriteProperty<>(0);
  public final ObservableProperty<String> label = new ObservableReadWriteProperty<>("");
  public final ObservableProperty<Boolean> visible = new ObservableReadWriteProperty<>(true);
  public final ObservableProperty<Boolean> indeterminate = new ObservableReadWriteProperty<>(true);

  public ProgressViewModel() {
    this.value.addSimpleObserver(this, ProgressViewModel::setVisible);
  }

  /**
   * Resets the progress and sets a new max value and label.
   */
  public void start(int maxValue, String label) {
    this.maxValue.set(maxValue);
    this.value.set(0);
    this.label.set(label);
  }

  public void increment() {
    this.value.set(getValue() + 1);
  }

  public void setVisible() {
    this.visible.set(getValue() < getMaxValue());
  }

  private int getValue() {
    return Optional.ofNullable(value.get()).orElse(0);
  }

  private int getMaxValue() {
    return Optional.ofNullable(maxValue.get()).orElse(0);
  }
}
