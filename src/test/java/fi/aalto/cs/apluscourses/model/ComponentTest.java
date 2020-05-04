package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import fi.aalto.cs.apluscourses.utils.Event;
import org.junit.Test;

public class ComponentTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testStateChanged() {
    Object listener = new Object();
    Event.Callback<Object> callback = mock(Event.Callback.class);
    Component component = new ModelExtensions.TestComponent("testComponent");
    component.stateChanged.addListener(listener, callback);
    verifyNoInteractions(callback);
    assertEquals(Component.UNRESOLVED, component.stateMonitor.get());
    component.stateMonitor.set(Component.FETCHING);
    verify(callback, times(1)).callbackUntyped(listener);
    verifyNoMoreInteractions(callback);
  }

  @Test
  public void testHasErrorReturnsTrue() {
    Component component = new ModelExtensions.TestComponent("errorComponent");
    component.stateMonitor.set(Component.ERROR);
    assertTrue("Component should have error when its state is ERROR", component.hasError());
  }

  @Test
  public void testHasErrorReturnsFalse() {
    Component component = new ModelExtensions.TestComponent("nonErrorComponent");
    component.stateMonitor.set(Component.NOT_INSTALLED);
    assertFalse("Component shouldn't have error when in a non-error state", component.hasError());
  }

  @Test
  public void testHasErrorReturnsTrueWhenErrorInDependencies() {
    Component component = new ModelExtensions.TestComponent("errorComponent");
    component.stateMonitor.set(Component.LOADED);
    component.dependencyStateMonitor.set(Component.DEP_ERROR);
    assertTrue("Component should have error when its state is LOADED and dependency state is error",
        component.hasError());
  }
}
