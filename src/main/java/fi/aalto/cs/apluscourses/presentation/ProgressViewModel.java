package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.Progress;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class ProgressViewModel {
  private final Deque<Progress> progresses = new ArrayDeque<>();
  public final ObservableProperty<Integer> maxValue = new ObservableReadWriteProperty<>(0);
  public final ObservableProperty<Integer> value = new ObservableReadWriteProperty<>(0);
  public final ObservableProperty<String> label =
          new ObservableReadWriteProperty<>(getText("ui.ProgressBarView.loading"));
  public final ObservableProperty<Boolean> visible = new ObservableReadWriteProperty<>(true);
  public final ObservableProperty<Boolean> indeterminate = new ObservableReadWriteProperty<>(true);

  public ProgressViewModel() {
    this.value.addSimpleObserver(this, ProgressViewModel::updateVisible);
    this.indeterminate.addSimpleObserver(this, ProgressViewModel::updateVisible);
  }

  /**
   * Creates a new Progress and returns it.
   */
  public Progress start(int maxValue, String label, boolean indeterminate) {
    var progress = new Progress(maxValue, label, indeterminate);
    progresses.add(progress);
    this.updateValues();
    return progress;
  }

  public void stop(Progress progress) {
    progresses.remove(progress);
    this.updateValues();
  }

  public void stopAll() {
    progresses.clear();
    this.updateValues();
  }

  private void updateValues() {
    if (progresses.isEmpty()) {
      this.maxValue.set(0);
      this.value.set(0);
      this.indeterminate.set(false);
    } else {
      var progress = progresses.getLast();
      this.value.set(progress.getValue());
      this.maxValue.set(progress.getMaxValue());
      this.label.set(progress.getLabel());
      this.indeterminate.set(progress.getIndeterminate());
    }
    this.updateVisible();
  }

  private void updateValue() {
    var progress = progresses.getLast();
    this.value.set(progress.getValue());
  }

  public void increment(Progress progress) {
    progress.increment();
    this.updateValue();
  }

  public void updateVisible() {
    this.visible.set(getValue() < getMaxValue() || isIndeterminate());
  }

  public Progress getCurrentProgress() {
    return progresses.isEmpty() ? null : progresses.getLast();
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
}
