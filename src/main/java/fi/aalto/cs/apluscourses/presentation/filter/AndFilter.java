package fi.aalto.cs.apluscourses.presentation.filter;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class AndFilter implements Filter {
  private final List<? extends Filter> filters;

  public AndFilter(List<? extends Filter> filters) {
    this.filters = filters;
  }

  @Override
  @NotNull
  public Optional<Boolean> apply(Object item) {
    boolean empty = true;
    for (Filter filter : filters) {
      Optional<Boolean> value = filter.apply(item);
      if (value.filter(Boolean.FALSE::equals).isPresent()) {
        return Optional.of(false);
      }
      if (value.isPresent()) {
        empty = false;
      }
    }
    return empty ? Optional.empty() : Optional.of(true);
  }
}
