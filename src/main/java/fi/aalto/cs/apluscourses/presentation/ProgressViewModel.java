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
  private final Object lock = new Object();
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
    synchronized (lock) {
      progresses.add(progress);
    }
    this.updateValues();
    return progress;
  }

  /**
   * Stops a Progress.
   */
  public void stop(Progress progress) {
    synchronized (lock) {
      progresses.remove(progress);
    }
    this.updateValues();
  }

  /**
   * Stops all Progresses.
   */
  public void stopAll() {
    synchronized (lock) {
      progresses.clear();
    }
    this.updateValues();
  }

  private void updateValues() {
    synchronized (lock) {
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
    }
    this.updateVisible();
  }

  private void updateValue() {
    synchronized (lock) {
      var progress = progresses.getLast();
      this.value.set(progress.getValue());
    }
  }

  /**
   * Increments a Progress and updates the value of the visible progress.
   */
  public void increment(Progress progress) {
    synchronized (lock) {
      progress.increment();
    }
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
