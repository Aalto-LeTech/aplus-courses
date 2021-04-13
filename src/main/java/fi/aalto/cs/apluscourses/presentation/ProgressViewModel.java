package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

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
    this.value.addSimpleObserver(this, ProgressViewModel::updateVisible);
    this.indeterminate.addSimpleObserver(this, ProgressViewModel::updateVisible);
  }

  /**
   * Resets the progress and sets a new max value and label. If the progress is visible, then the
   * label gets set to Loading... and the max value is increased.
   */
  public void start(int maxValue, String label) {
    if (isVisible() && !label.equals(this.label.get())) {
      this.maxValue.set(getMaxValue() + maxValue);
      this.label.set(getText("ui.ProgressBarView.loading"));
    } else {
      this.maxValue.set(maxValue);
      this.value.set(0);
      this.label.set(label);
    }
  }

  /**
   * Resets the values and sets a label and indeterminate to true.
   */
  public void start(String label) {
    if (isVisible()) {
      this.label.set(getText("ui.ProgressBarView.loading"));
    } else {
      this.maxValue.set(0);
      this.value.set(0);
      this.label.set(label);
    }
    this.indeterminate.set(true);
  }

  /**
   * Sets the values to 0 and indeterminate to false.
   */
  public void stop() {
    this.maxValue.set(0);
    this.value.set(0);
    this.indeterminate.set(false);
  }

  public void increment() {
    this.value.set(getValue() + 1);
  }

  public void updateVisible() {
    this.visible.set(getValue() < getMaxValue() || isIndeterminate());
  }

  private int getValue() {
    return Optional.ofNullable(value.get()).orElse(0);
  }

  private int getMaxValue() {
    return Optional.ofNullable(maxValue.get()).orElse(0);
  }

  private boolean isIndeterminate() {
    return Optional.ofNullable(this.indeterminate.get()).orElse(false);
  }

  private boolean isVisible() {
    return Optional.ofNullable(this.visible.get()).orElse(false);
  }
}
