package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.ViewModelExtensions.TestNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SelectableNodeViewModelTest {

  Object model;
  SelectableNodeViewModel<Object> node;
  SelectableNodeViewModel<Object> child0;
  SelectableNodeViewModel<Object> child1;
  SelectableNodeViewModel<Object> child2;
  List<SelectableNodeViewModel<?>> children;

  /**
   * Run before each call.
   */
  @Before
  public void setUp() {
    model = new Object();

    child0 = spy(new TestNodeViewModel(0, new Object(), Collections.emptyList()));
    child1 = spy(new TestNodeViewModel(1, new Object(), Collections.emptyList()));
    child2 = spy(new TestNodeViewModel(2, new Object(), Collections.emptyList()));
    children = Arrays.asList(child0, child1, child2);
    node = new TestNodeViewModel(3, model, children);
  }

  @Test
  public void testGetModel() {
    assertSame(model, node.getModel());
  }

  @Test
  public void testGetChildren() {
    List<SelectableNodeViewModel<?>> actualChildren = node.getChildren();
    assertEquals(3, actualChildren.size());
    assertSame(child0, actualChildren.get(0));
    assertSame(child1, actualChildren.get(1));
    assertSame(child2, actualChildren.get(2));
  }

  @Test(expected = InterruptedException.class)
  public void testApplyFilterIsInterrupted() throws InterruptedException {
    Filter filter = mock(Filter.class);

    Thread.currentThread().interrupt();
    node.applyFilter(filter);
  }

  @Test
  public void testApplyFilterWhenChildReturnsTrue() throws InterruptedException {
    Filter filter = mock(Filter.class);
    when(filter.apply(child0)).thenReturn(Optional.of(false));
    when(filter.apply(child1)).thenReturn(Optional.of(true));
    when(filter.apply(child2)).thenReturn(Optional.empty());


    Optional<Boolean> result = node.applyFilter(filter);
    assertTrue(result.isPresent() && Boolean.TRUE.equals(result.get()));
    assertTrue(node.isVisible());
  }

  @Test
  public void testApplyFilterReturnsFalse() throws InterruptedException {
    Filter filter = mock(Filter.class, new Returns(Optional.empty()));
    when(filter.apply(node)).thenReturn(Optional.of(false));

    Optional<Boolean> result = node.applyFilter(filter);
    assertTrue(result.isPresent() && Boolean.FALSE.equals(result.get()));
    assertFalse(node.isVisible());
  }

  @Test
  public void testApplyFilterReturnsEmpty() throws InterruptedException {
    Filter filter = mock(Filter.class, new Returns(Optional.empty()));

    Optional<Boolean> result = node.applyFilter(filter);
    assertFalse(result.isPresent());
    assertTrue(node.isVisible());
  }
}
