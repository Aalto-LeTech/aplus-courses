package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import fi.aalto.cs.apluscourses.utils.Event;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComponentTest {
  @SuppressWarnings("unchecked")
  @Test
  void testStateChanged() {
    Object listener = new Object();
    Event.Callback<Object> callback = mock(Event.Callback.class);
    Component component = new ModelExtensions.TestComponent();

    component.stateChanged.addListener(listener, callback);

    verifyNoInteractions(callback);
    Assertions.assertEquals(Component.UNRESOLVED, component.stateMonitor.get(),
        "Component should be initially in UNRESOLVED state");
    component.stateMonitor.set(Component.FETCHING);
    verify(callback).callbackUntyped(listener, null);
    verifyNoMoreInteractions(callback);
  }

  @Test
  void testGetName() {
    String componentName = "testComponent";
    Component component = new ModelExtensions.TestComponent(componentName);
    Assertions.assertEquals(componentName, component.getName(),
        "The name should be the same as was given to constructor");
  }

  @Test
  void testHasErrorReturnsTrue() {
    Component component = new ModelExtensions.TestComponent();
    component.stateMonitor.set(Component.ERROR);
    Assertions.assertTrue(component.hasError(), "Component should have error when its state is ERROR");
  }

  @Test
  void testHasErrorReturnsFalse() {
    Component component = new ModelExtensions.TestComponent();
    component.stateMonitor.set(Component.NOT_INSTALLED);
    Assertions.assertFalse(component.hasError(), "Component shouldn't have error when in a non-error state");
  }

  @Test
  void testHasErrorWhenLoaded() {
    Component component = new ModelExtensions.TestComponent();
    component.stateMonitor.set(Component.LOADED);
    component.dependencyStateMonitor.set(Component.DEP_ERROR);
    Assertions.assertTrue(component.hasError(),
        "Component should have error when its state is LOADED and dependency state is error");
    component.dependencyStateMonitor.set(Component.DEP_LOADED);
    Assertions.assertFalse(component.hasError(),
        "Component shouldn't have error when its state and dependency state are ok");
  }

  @Test
  void testResolveState() {
    int resolvedState = Component.LOADED;
    Component component = spy(new ModelExtensions.TestComponent());
    when(component.resolveStateInternal()).thenReturn(resolvedState);

    Assertions.assertEquals(Component.UNRESOLVED, component.stateMonitor.get(),
        "Component should be initially in UNRESOLVED state");

    component.resolveState();

    Assertions.assertEquals(resolvedState, component.stateMonitor.get(),
        "Component's state should be one that was resolved by the subclass");

    component.resolveState();

    Assertions.assertEquals(resolvedState, component.stateMonitor.get(),
        "Component's state should be the same as in the previous call");

    verify(component).resolveStateInternal();
  }

  @Test
  void testSetUnresolved() {
    Component component = new ModelExtensions.TestComponent();
    component.stateMonitor.set(Component.LOADED);
    component.setUnresolved();
    Assertions.assertEquals(Component.UNRESOLVED, component.stateMonitor.get(),
        "setUnresolved() should set the component to UNRESOLVED state");
  }

  @Test
  void testGetDependencies() {
    List<String> dependencies = List.of("dep1", "dep2", "dep3");
    Component component = spy(new ModelExtensions.TestComponent());
    when(component.computeDependencies()).thenReturn(dependencies);

    MatcherAssert.assertThat("getDependencies() should return the dependencies computed by the subclass",
        component.getDependencies(), is(dependencies));
    MatcherAssert.assertThat("Subsequent call should return the same dependencies", component.getDependencies(),
        is(dependencies));

    verify(component).computeDependencies();
  }

  @Test
  void testValidateTrivialCases() {
    Component component = new ModelExtensions.TestComponent();
    ComponentSource componentSource = mock(ComponentSource.class);

    component.validate(componentSource);

    component.stateMonitor.set(Component.LOADED);

    component.validate(componentSource);

    verifyNoInteractions(componentSource);
  }

  @Test
  void testValidate() {
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
        return List.of("dependency1", "dependency2");
      }
    };
    component.stateMonitor.set(Component.LOADED);
    component.dependencyStateMonitor.set(Component.DEP_ERROR);
    component.validate(components::get);

    Assertions.assertEquals(Component.DEP_LOADED, component.dependencyStateMonitor.get(),
        "Component's dependency state should be DEP_LOADED");
  }

  @Test
  void testValidateInvalid() {
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

    Assertions.assertEquals(Component.DEP_ERROR, component.dependencyStateMonitor.get(),
        "Component's dependency state should be DEP_ERROR");
  }

  @Test
  void testValidateDependencyMissing() {
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

    Assertions.assertEquals(Component.DEP_ERROR, component.dependencyStateMonitor.get(),
        "Component's dependency state should be DEP_ERROR");
  }
}
