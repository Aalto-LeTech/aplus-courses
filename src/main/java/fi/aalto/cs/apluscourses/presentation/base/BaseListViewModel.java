package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import fi.aalto.cs.apluscourses.presentation.filter.FilterEngine;
import fi.aalto.cs.apluscourses.presentation.filter.Filterable;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for list view models.
 *
 * @param <E> Type of the list elements (presentation models), subtype of
 *            {@link ListElementViewModel}
 */
public class BaseListViewModel<E extends ListElementViewModel<?>> implements Filterable {
  private final @NotNull FilterEngine filterEngine;
  protected final @NotNull List<E> elements;

  /**
   * A constructor.
   *
   * @param models                      A List of model elements.
   * @param listElementViewModelFactory A function that creates a list element view model object of
   *                                    a model object.
   */
  public <M> BaseListViewModel(@NotNull List<M> models,
                               @NotNull Options options,
                               @NotNull Function<M, E> listElementViewModelFactory) {
    this.elements = models.stream().map(listElementViewModelFactory).collect(Collectors.toList());
    filterEngine = new FilterEngine(options, this);
    filterEngine.filter();
  }

  public Stream<E> streamVisibleItems() {
    return elements.stream().filter(ListElementViewModel::isVisible);
  }

  /**
   * Get currently selected elements as a list.  The list is a snapshot of the current selection
   * state and is not updated after this method call.
   *
   * @return A {@link List}.
   */
  public List<E> getSelectedElements() {
    return streamSelectedElements().collect(Collectors.toList());
  }

  private Stream<E> streamSelectedElements() {
    return elements.stream().filter(ListElementViewModel::isSelected);
  }

  public @NotNull FilterEngine getFilterEngine() {
    return filterEngine;
  }

  @Override
  public void applyFilter(@NotNull Filter filter) throws InterruptedException {
    for (var elem : elements) {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      elem.applyFilter(filter);
    }
  }

  public boolean isSelectionEmpty() {
    return elements.stream().filter(ListElementViewModel::isSelected).findAny().isEmpty();
  }

  public boolean isSingleSelection() {
    return CollectionUtil.lengthEquals(streamSelectedElements(), 1);
  }
}
