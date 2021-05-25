package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.Progress;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.ArrayDeque;
import java.util.Collection;
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
    progress.updated.addListener(this, ProgressViewModel::updateValues);
    return progress;
  }

  /**
   * Stops all Progresses.
   */
  public void stopAll() {
    synchronized (lock) {
      progresses.forEach(Progress::finish);
    }
    this.updateValues();
  }

  private void updateValues() {
    Collection<Progress> removed;
    synchronized (lock) {
      removed = CollectionUtil.removeIf(progresses, Progress::isFinished);
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
    removed.forEach(this::unregisterProgress);
    this.updateVisible();
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

  private void unregisterProgress(Progress progress) {
    progress.updated.removeCallback(this);
  }
}
