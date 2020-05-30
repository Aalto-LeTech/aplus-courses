package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import fi.aalto.cs.apluscourses.utils.Event;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ComponentTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testStateChanged() {
    Object listener = new Object();
    Event.Callback<Object> callback = mock(Event.Callback.class);
    Component component = new ModelExtensions.TestComponent();

    component.stateChanged.addListener(listener, callback);

    verifyNoInteractions(callback);
    assertEquals("Component should be initially in UNRESOLVED state",
        Component.UNRESOLVED, component.stateMonitor.get());
    component.stateMonitor.set(Component.FETCHING);
    verify(callback, times(1)).callbackUntyped(listener);
    verifyNoMoreInteractions(callback);
  }

  @Test
  public void testGetName() {
    String componentName = "testComponent";
    Component component = new ModelExtensions.TestComponent(componentName);
    assertEquals("The name should be the same as was given to constructor",
        componentName, component.getName());
  }

  @Test
  public void testIsActive() {
    List<Integer> inactiveStates = Arrays.asList(
        Component.NOT_INSTALLED, Component.FETCHED, Component.LOADED,
        Component.ERROR, Component.UNRESOLVED);
    List<Integer> activeStates = Arrays.asList(Component.FETCHING, Component.LOADING);

    Component component = new ModelExtensions.TestComponent();

    for (Integer activeState : activeStates) {
      component.stateMonitor.set(activeState);
      assertTrue("Component should be active", component.isActive());
    }

    component = new ModelExtensions.TestComponent();

    for (Integer inactiveState : inactiveStates) {
      component.stateMonitor.set(inactiveState);
      assertFalse("Component should not be active", component.isActive());
    }

    component.dependencyStateMonitor.set(Component.DEP_WAITING);
    assertTrue("Component should be active", component.isActive());
  }

  @Test
  public void testHasErrorReturnsTrue() {
    Component component = new ModelExtensions.TestComponent();
    component.stateMonitor.set(Component.ERROR);
    assertTrue("Component should have error when its state is ERROR", component.hasError());
  }

  @Test
  public void testHasErrorReturnsFalse() {
    Component component = new ModelExtensions.TestComponent();
    component.stateMonitor.set(Component.NOT_INSTALLED);
    assertFalse("Component shouldn't have error when in a non-error state", component.hasError());
  }

  @Test
  public void testHasErrorWhenLoaded() {
    Component component = new ModelExtensions.TestComponent();
    component.stateMonitor.set(Component.LOADED);
    component.dependencyStateMonitor.set(Component.DEP_ERROR);
    assertTrue("Component should have error when its state is LOADED and dependency state is error",
        component.hasError());
    component.dependencyStateMonitor.set(Component.DEP_LOADED);
    assertFalse("Component shouldn't have error when its state and dependency state are ok",
        component.hasError());
  }

  @Test
  public void testResolveState() {
    int resolvedState = Component.LOADED;
    Component component = spy(new ModelExtensions.TestComponent());
    when(component.resolveStateInternal()).thenReturn(resolvedState);

    assertEquals("Component should be initially in UNRESOLVED state",
        Component.UNRESOLVED, component.stateMonitor.get());

    component.resolveState();

    assertEquals("Component's state should be one that was resolved by the subclass",
        resolvedState, component.stateMonitor.get());

    component.resolveState();

    assertEquals("Component's state should be the same as in the previous call",
        resolvedState, component.stateMonitor.get());

    verify(component).resolveStateInternal();
  }

  @Test
  public void testSetUnresolved() {
    Component component = new ModelExtensions.TestComponent();
    component.stateMonitor.set(Component.LOADED);
    component.setUnresolved();
    assertEquals("setUnresolved() should set the component to UNRESOLVED state",
        Component.UNRESOLVED, component.stateMonitor.get());
  }

  @Test
  public void testGetDependencies() {
    List<String> dependencies = Arrays.asList("dep1", "dep2", "dep3");
    Component component = spy(new ModelExtensions.TestComponent());
    when(component.computeDependencies()).thenReturn(dependencies);

    assertThat("getDependencies() should return the dependencies computed by the subclass",
        component.getDependencies(), is(dependencies));
    assertThat("Subsequent call should return the same dependencies",
        component.getDependencies(), is(dependencies));

    verify(component).computeDependencies();
  }

  @Test
  public void testValidateTrivialCases() {
    Component component = new ModelExtensions.TestComponent();
    ComponentSource componentSource = mock(ComponentSource.class);

    component.validate(componentSource);

    component.stateMonitor.set(Component.LOADED);

    component.validate(componentSource);

    verifyNoInteractions(componentSource);
  }

  @Test
  public void testValidate() {
    Map<String, Component> components = new HashMap<>();

    Component dependency1 = new ModelExtensions.TestComponent();
    dependency1.stateMonitor.set(Component.LOADED);
    components.put("dependency1", dependency1);

    Component dependency2 = new ModelExtensions.TestComponent();
    dependency2.stateMonitor.set(Component.LOADED);
    components.put("dependency2", dependency2);

    Component component = new ModelExtensions.TestComponent() {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
        return Arrays.asList("dependency1", "dependency2");
      }
    };
    component.stateMonitor.set(Component.LOADED);
    component.dependencyStateMonitor.set(Component.DEP_ERROR);
    component.validate(components::get);

    assertEquals("Component's dependency state should be DEP_LOADED",
        Component.DEP_LOADED, component.dependencyStateMonitor.get());
  }

  @Test
  public void testValidateInvalid() {
    Component nonLoadedDependency = new ModelExtensions.TestComponent();

    Component component = new ModelExtensions.TestComponent() {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
        return Collections.singletonList("nonLoadedDependency");
      }
    };
    component.stateMonitor.set(Component.LOADED);
    component.dependencyStateMonitor.set(Component.DEP_LOADED);
    component.validate(componentName -> nonLoadedDependency);

    assertEquals("Component's dependency state should be DEP_ERROR",
        Component.DEP_ERROR, component.dependencyStateMonitor.get());
  }

  @Test
  public void testValidateDependencyMissing() {
    Component component = new ModelExtensions.TestComponent() {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
        return Collections.singletonList("nonExistentDependency");
      }
    };
    component.stateMonitor.set(Component.LOADED);
    component.dependencyStateMonitor.set(Component.DEP_LOADED);
    component.validate(componentName -> null);

    assertEquals("Component's dependency state should be DEP_ERROR",
        Component.DEP_ERROR, component.dependencyStateMonitor.get());
  }
}
