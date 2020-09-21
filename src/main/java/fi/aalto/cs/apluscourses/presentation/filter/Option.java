package fi.aalto.cs.apluscourses.presentation.filter;

import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.Optional;
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

  public final ObservableProperty<Boolean> isSelected;

  /**
   * An option, that is, a filter that when not selected, filters out those that match to the filter
   * given to this constructor.
   */
  public Option(@NotNull String name,
                @Nullable Icon icon,
                @NotNull Filter filter) {
    this.icon = icon;
    this.name = name;
    this.filter = filter;
    isSelected = new ObservableReadWriteProperty<>(null);
  }

  /**
   * Should be called before the option is used. Subsequent calls are allowed.
   * Subclasses may override this.
   *
   * @return This instance, for fluency.
   */
  public Option init() {
    isSelected.set(true);
    return this;
  }

  @Override
  @NotNull
  public Optional<Boolean> apply(Object item) {
    return Boolean.TRUE.equals(isSelected.get())
        ? Optional.empty()
        : filter.apply(item).map(Boolean.FALSE::equals);
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
