package fi.aalto.cs.apluscourses.presentation.filter;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface Filter {
  /**
   * Resolves whether or not the filter applies to an item.
   *
   * @param item An item to check.
   * @return True: this filter applies to the item.
   * False: this filter could apply to the item, but does not.
   * Empty: this filter is not applicable to the item.
   */
  @NotNull
  Optional<Boolean> apply(Object item);
}
