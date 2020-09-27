package fi.aalto.cs.apluscourses.presentation.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;

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
    child0 = spy(new SelectableNodeViewModel<>(new Object(), Collections.emptyList()));
    child1 = spy(new SelectableNodeViewModel<>(new Object(), Collections.emptyList()));
    child2 = spy(new SelectableNodeViewModel<>(new Object(), Collections.emptyList()));
    children = Arrays.asList(child0, child1, child2);
    node = new SelectableNodeViewModel<>(model, children);
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

  @Test
  public void testSelection() {
    assertEquals(true, node.isVisible.get());

    Filterable.Listener listener = mock(Filterable.Listener.class);
    node.addVisibilityListener(listener);
    verify(listener).visibilityChanged(true);

    node.isVisible.set(true);
    assertEquals(true, node.isVisible.get());

    node.isVisible.set(false);
    assertEquals(false, node.isVisible.get());
    verify(listener).visibilityChanged(false);

    verifyNoMoreInteractions(listener);
  }

  @Test
  public void testApplyFilterIsInterrupted() throws InterruptedException {
    Filter filter = mock(Filter.class);

    Thread thread = new Thread(() -> {
      Thread.currentThread().interrupt();
      node.applyFilter(filter);
    });
    thread.start();
    thread.join();

    verify(child0, never()).applyFilter(any());
    verify(child1, never()).applyFilter(any());
    verify(child2, never()).applyFilter(any());
  }

  @Test
  public void testApplyFilterWhenChildReturnsTrue() {
    Filter filter = mock(Filter.class);
    when(filter.apply(child0)).thenReturn(Optional.of(false));
    when(filter.apply(child1)).thenReturn(Optional.of(true));
    when(filter.apply(child2)).thenReturn(Optional.empty());


    Optional<Boolean> result = node.applyFilter(filter);
    assertTrue(result.isPresent());
    assertTrue(result.get());
    assertEquals(true, node.isVisible.get());
  }

  @Test
  public void testApplyFilterReturnsFalse() {
    Filter filter = mock(Filter.class, new Returns(Optional.empty()));
    when(filter.apply(node)).thenReturn(Optional.of(false));

    Optional<Boolean> result = node.applyFilter(filter);
    assertTrue(result.isPresent());
    assertFalse(result.get());
    assertEquals(false, node.isVisible.get());
  }

  @Test
  public void testApplyFilterReturnsEmpty() {
    Filter filter = mock(Filter.class, new Returns(Optional.empty()));

    Optional<Boolean> result = node.applyFilter(filter);
    assertFalse(result.isPresent());
    assertEquals(true, node.isVisible.get());
  }
}
