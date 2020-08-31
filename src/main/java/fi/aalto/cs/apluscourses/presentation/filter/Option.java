package fi.aalto.cs.apluscourses.presentation.filter;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Option implements Filter {

  @NotNull
  private final String name;
  @Nullable
  private final Icon icon;

  public final ObservableProperty<Boolean> isSelected = new ObservableReadWriteProperty<>(true);

  public Option(@NotNull String name, @Nullable Icon icon) {
    this.icon = icon;
    this.name = name;
  }

  @Override
  public boolean apply(Object item) {
    return Boolean.TRUE.equals(isSelected.get()) && applyInternal(item);
  }

  protected abstract boolean applyInternal(Object item);

  @NotNull
  public String getName() {
    return name;
  }

  @Nullable
  public Icon getIcon() {
    return icon;
  }
}
