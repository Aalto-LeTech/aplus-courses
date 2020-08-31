package fi.aalto.cs.apluscourses.presentation.filter;

import java.util.List;

public class AndFilter implements Filter {
  private List<? extends Filter> filters;

  public AndFilter(List<? extends Filter> filters) {
    this.filters = filters;
  }

  @Override
  public boolean apply(Object item) {
    return filters.parallelStream().allMatch(filter -> filter.apply(item));
  }
}
