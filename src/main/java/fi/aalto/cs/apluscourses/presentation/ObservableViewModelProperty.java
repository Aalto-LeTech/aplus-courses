package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObservableViewModelProperty<M, V extends BaseViewModel<M>>
    extends ObservableReadWriteProperty<V> {

  @NotNull
  private final ModelSetter<M> modelSetter;

  public ObservableViewModelProperty(@Nullable V initialValue,
                                     @NotNull ModelSetter<M> modelSetter) {
    super(initialValue);
    this.modelSetter = modelSetter;
  }

  @Override
  protected boolean setInternal(V newValue) {
    if (super.setInternal(newValue)) {
      modelSetter.setModel(newValue.getModel());
      return true;
    }
    return false;
  }

  @FunctionalInterface
  public interface ModelSetter<M> {
    void setModel(M model);
  }
}
