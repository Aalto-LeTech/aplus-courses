package fi.aalto.cs.apluscourses.presentation.filter;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Option implements Filter {

  @NotNull
  private final String name;
  @NotNull
  private final Filter filter;
  @Nullable
  private final Icon icon;

  public final ObservableProperty<Boolean> isSelected = new ObservableReadWriteProperty<>(true);

  public Option(@NotNull String name, @Nullable Icon icon, @NotNull Filter filter) {
    this.icon = icon;
    this.name = name;
    this.filter = filter;
  }

  @Override
  public boolean apply(Object item) {
    return Boolean.TRUE.equals(isSelected.get()) && filter.apply(item);
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
