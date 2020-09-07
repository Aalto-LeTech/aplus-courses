package fi.aalto.cs.apluscourses.presentation.filter;

import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import org.jetbrains.annotations.NotNull;

public class LogicalFilter implements Filter {
  private final List<? extends Filter> filters;
  private final BinaryOperator<Boolean> operator;

  public LogicalFilter(List<? extends Filter> filters, BinaryOperator<Boolean> operator) {
    this.filters = filters;
    this.operator = operator;
  }

  @Override
  @NotNull
  public Optional<Boolean> apply(Object item) {
    return filters.stream()
        .map(filter -> filter.apply(item))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .reduce(operator);
  }

  public static class Conjunction extends LogicalFilter {
    public Conjunction(List<? extends Filter> filters) {
      super(filters, Boolean::logicalAnd);
    }
  }
}
