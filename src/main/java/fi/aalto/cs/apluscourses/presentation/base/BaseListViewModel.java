package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.RestartableTask;
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
public class BaseListViewModel<E extends ListElementViewModel<?>> {

  public final Event filtered = new Event();

  private final transient @NotNull RestartableTask filterTask;
  private final @NotNull Options options;
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
    this.filterTask = new RestartableTask(this::doFilter, filtered::trigger);
    this.options = options;
    options.optionsChanged.addListener(this, BaseListViewModel::filter);
    filter();
  }

  private void filter() {
    filterTask.restart();
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
    return elements
        .stream()
        .filter(ListElementViewModel::isSelected)
        .collect(Collectors.toList());
  }

  private void doFilter() throws InterruptedException {
    for (var elem : elements) {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
      elem.applyFilter(options);
    }
  }

  public boolean isSelectionEmpty() {
    return elements.stream().filter(ListElementViewModel::isSelected).findAny().isEmpty();
  }
}
