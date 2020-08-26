package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseFilter<T> {

  @NotNull
  public abstract Option[] getOptions();

  public abstract boolean apply(T item);

  public static class Option {
    @NotNull
    private final String name;
    @Nullable
    private final Icon icon;

    public ObservableProperty<Boolean> isSelected = new ObservableReadWriteProperty<>(true);

    public Option(@NotNull String name, @Nullable Icon icon) {
      this.icon = icon;
      this.name = name;
    }

    @NotNull
    public String getName() {
      return name;
    }

    @Nullable
    public Icon getIcon() {
      return icon;
    }
  }
}
