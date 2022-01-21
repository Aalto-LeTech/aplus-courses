package fi.aalto.cs.apluscourses.presentation.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import fi.aalto.cs.apluscourses.presentation.ViewModelExtensions.TestNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.Returns;

class SelectableNodeViewModelTest {

  Object model;
  SelectableNodeViewModel<Object> node;
  SelectableNodeViewModel<Object> child0;
  SelectableNodeViewModel<Object> child1;
  SelectableNodeViewModel<Object> child2;
  List<SelectableNodeViewModel<?>> children;

  /**
   * Run before each call.
   */
  @BeforeEach
  void setUp() {
    model = new Object();

    child0 = spy(new TestNodeViewModel(0, new Object(), Collections.emptyList()));
    child1 = spy(new TestNodeViewModel(1, new Object(), Collections.emptyList()));
    child2 = spy(new TestNodeViewModel(2, new Object(), Collections.emptyList()));
    children = List.of(child0, child1, child2);
    node = new TestNodeViewModel(3, model, children);
  }

  @Test
  void testGetModel() {
    Assertions.assertSame(model, node.getModel());
  }

  @Test
  void testGetChildren() {
    List<SelectableNodeViewModel<?>> actualChildren = node.getChildren();
    Assertions.assertEquals(3, actualChildren.size());
    Assertions.assertSame(child0, actualChildren.get(0));
    Assertions.assertSame(child1, actualChildren.get(1));
    Assertions.assertSame(child2, actualChildren.get(2));
  }

  @Test
  void testApplyFilterIsInterrupted() throws InterruptedException {
    Filter filter = mock(Filter.class);

    Thread.currentThread().interrupt();

    assertThrows(InterruptedException.class, () ->
        node.applyFilter(filter));
  }

  @Test
  void testApplyFilterWhenChildReturnsTrue() throws InterruptedException {
    Filter filter = mock(Filter.class);
    when(filter.apply(child0)).thenReturn(Optional.of(false));
    when(filter.apply(child1)).thenReturn(Optional.of(true));
    when(filter.apply(child2)).thenReturn(Optional.empty());


    Optional<Boolean> result = node.applyFilter(filter);
    Assertions.assertTrue(result.isPresent() && Boolean.TRUE.equals(result.get()));
    Assertions.assertTrue(node.isVisible());
  }

  @Test
  void testApplyFilterReturnsFalse() throws InterruptedException {
    Filter filter = mock(Filter.class, new Returns(Optional.empty()));
    when(filter.apply(node)).thenReturn(Optional.of(false));

    Optional<Boolean> result = node.applyFilter(filter);
    Assertions.assertTrue(result.isPresent() && Boolean.FALSE.equals(result.get()));
    Assertions.assertFalse(node.isVisible());
  }

  @Test
  void testApplyFilterReturnsEmpty() throws InterruptedException {
    Filter filter = mock(Filter.class, new Returns(Optional.empty()));

    Optional<Boolean> result = node.applyFilter(filter);
    Assertions.assertFalse(result.isPresent());
    Assertions.assertTrue(node.isVisible());
  }
}
