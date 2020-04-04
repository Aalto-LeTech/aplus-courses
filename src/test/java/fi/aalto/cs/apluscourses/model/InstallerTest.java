package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.InOrder;

public class InstallerTest {

  @Test
  public void testInstall() throws IOException, ComponentLoadException {
    Module module = spy(new ModelExtensions.TestModule("someModule") {
      @Override
      public void fetch() {
        assertEquals("When fetch() is called, module should be in FETCHING state.",
            Component.FETCHING, stateMonitor.get());
      }

      @Override
      public void load() {
        assertEquals("When load() is called, module should be in LOADING state.",
            Component.LOADING, stateMonitor.get());
      }
    });

    Installer installer =
        new InstallerImpl<>(mock(ComponentSource.class), new SimpleAsyncTaskManager());

    installer.install(module);

    InOrder order = inOrder(module);
    order.verify(module).fetch();
    order.verify(module).load();

    assertEquals("Module should be in INSTALLED state, after the installation has ended.",
        Component.INSTALLED, module.stateMonitor.get());
  }

  @Test
  public void testInstallDependencies()
      throws IOException, ComponentLoadException, NoSuchModuleException {
    Module module1 = spy(new ModelExtensions.TestModule("dependentModule") {
      @NotNull
      @Override
      public List<String> getDependencies() {
        assertThat("Module should be at least in FETCHED state when getDependencies() is called.",
            stateMonitor.get(), greaterThanOrEqualTo(Component.FETCHED));
        List<String> dependencies = new ArrayList<>();
        dependencies.add("firstDep");
        dependencies.add("secondDep");
        return dependencies;
      }
    });
    Module firstDep = spy(new ModelExtensions.TestModule("firstDep"));
    Module secondDep = spy(new ModelExtensions.TestModule("secondDep"));

    ComponentSource componentSource = mock(ComponentSource.class);
    when(componentSource.getComponent(module1.getName())).thenReturn(module1);
    when(componentSource.getComponent(firstDep.getName())).thenReturn(firstDep);
    when(componentSource.getComponent(secondDep.getName())).thenReturn(secondDep);

    Installer installer =
        new InstallerImpl<>(componentSource, new SimpleAsyncTaskManager());

    installer.install(module1);

    InOrder order1 = inOrder(module1);
    order1.verify(module1).fetch();
    order1.verify(module1).load();

    InOrder order2 = inOrder(firstDep);
    order2.verify(firstDep).fetch();
    order2.verify(firstDep).load();

    InOrder order3 = inOrder(secondDep);
    order3.verify(secondDep).fetch();
    order3.verify(secondDep).load();

    assertEquals("Dependent module should be in INSTALLED state, after the installation has ended.",
        Component.INSTALLED, module1.stateMonitor.get());

    assertThat("1st dependency should be in LOADED state or further.",
        firstDep.stateMonitor.get(), greaterThanOrEqualTo(Component.LOADED));

    assertThat("2nd dependency should be in LOADED state or further.",
        secondDep.stateMonitor.get(), greaterThanOrEqualTo(Component.LOADED));
  }

  @Test
  public void testInstallMany() throws IOException, ComponentLoadException, NoSuchModuleException {
    Module module1 = spy(new ModelExtensions.TestModule("module1"));
    Module module2 = spy(new ModelExtensions.TestModule("module2"));

    ComponentSource componentSource = mock(ComponentSource.class);
    when(componentSource.getComponent(module1.getName())).thenReturn(module1);
    when(componentSource.getComponent(module2.getName())).thenReturn(module2);

    Installer installer =
        new InstallerImpl<>(componentSource, new SimpleAsyncTaskManager());

    List<Component> modules = new ArrayList<>();
    modules.add(module1);
    modules.add(module2);

    installer.install(modules);

    InOrder order1 = inOrder(module1);
    order1.verify(module1).fetch();
    order1.verify(module1).load();
    InOrder order2 = inOrder(module2);
    order2.verify(module2).fetch();
    order2.verify(module2).load();

    assertEquals("Module 1 should be in INSTALLED state, after the installation has ended.",
        Component.INSTALLED, module1.stateMonitor.get());

    assertEquals("Module 2 should be in INSTALLED state, after the installation has ended.",
        Component.INSTALLED, module2.stateMonitor.get());
  }

  @Test
  public void testInstallFetchFails() throws ComponentLoadException {
    Module module = spy(new ModelExtensions.TestModule("fetchFailModule") {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });

    Installer installer =
        new InstallerImpl<>(mock(ComponentSource.class), new SimpleAsyncTaskManager());

    installer.install(module);

    verify(module, never()).getDependencies();
    verify(module, never()).load();

    assertTrue("Fetch-fail-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallLoadFails() {
    Module module = spy(new ModelExtensions.TestModule("loadFailModule") {
      @Override
      public void load() throws ComponentLoadException {
        throw new ComponentLoadException(this, null);
      }
    });

    Installer installer =
        new InstallerImpl<>(mock(ComponentSource.class), new SimpleAsyncTaskManager());

    installer.install(module);

    assertTrue("Load-fail-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallUnknownDependency() throws ComponentLoadException {
    Module module = spy(new ModelExtensions.TestModule("unknownDepModule") {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("nonExistentModule");
      }
    });

    ComponentSource componentSource = componentName -> {
      throw new NoSuchModuleException(componentName, null);
    };

    Installer installer =
        new InstallerImpl<>(componentSource, new SimpleAsyncTaskManager());

    installer.install(module);

    verify(module, never()).load();

    assertTrue("Unknown-dep-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallGetDependenciesFail() throws ComponentLoadException {
    Module module = spy(new ModelExtensions.TestModule("failingDepModule") {
      @NotNull
      @Override
      public List<String> getDependencies() throws ComponentLoadException {
        throw new ComponentLoadException(this, null);
      }
    });

    Installer installer =
        new InstallerImpl<>(mock(ComponentSource.class), new SimpleAsyncTaskManager());

    installer.install(module);

    verify(module, never()).load();

    assertTrue("Unknown-dep-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallDependencyFails() throws NoSuchModuleException {
    Module dependentModule = spy(new ModelExtensions.TestModule("dependentModule") {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("failingDep");
      }
    });
    Module otherModule = spy(new ModelExtensions.TestModule("otherModule"));
    Module failingDep = spy(new ModelExtensions.TestModule("failingDep") {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });

    ComponentSource componentSource = mock(ComponentSource.class);
    when(componentSource.getComponent(dependentModule.getName())).thenReturn(dependentModule);
    when(componentSource.getComponent(otherModule.getName())).thenReturn(otherModule);
    when(componentSource.getComponent(failingDep.getName())).thenReturn(failingDep);

    Installer installer =
        new InstallerImpl<>(componentSource, new SimpleAsyncTaskManager());

    List<Component> modules = new ArrayList<>();
    modules.add(dependentModule);
    modules.add(otherModule);

    installer.install(modules);

    assertEquals("Dependent module should be in INSTALLED state, after the installation has ended.",
        Component.INSTALLED, dependentModule.stateMonitor.get());
    assertEquals("Other module should be in INSTALLED state, after the installation has ended.",
        Component.INSTALLED, otherModule.stateMonitor.get());
    assertTrue("Failing dependency should be in an error state, after the installation has ended.",
        failingDep.hasError());
  }

  @Test
  public void testInstallCircularDependency() throws NoSuchModuleException {
    Module moduleA = spy(new ModelExtensions.TestModule("moduleA") {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("moduleB");
      }
    });
    Module moduleB = spy(new ModelExtensions.TestModule("moduleB") {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("moduleA");
      }
    });

    ComponentSource componentSource = mock(ComponentSource.class);
    when(componentSource.getComponent(moduleA.getName())).thenReturn(moduleA);
    when(componentSource.getComponent(moduleB.getName())).thenReturn(moduleB);

    Installer installer =
        new InstallerImpl<>(componentSource, new SimpleAsyncTaskManager());

    List<Component> modules = new ArrayList<>();
    modules.add(moduleA);
    modules.add(moduleB);

    installer.install(modules);

    assertEquals("Module A should be in INSTALLED state, after the installation has ended.",
        Component.INSTALLED, moduleA.stateMonitor.get());
    assertEquals("Module B should be in INSTALLED state, after the installation has ended.",
        Component.INSTALLED, moduleB.stateMonitor.get());
  }
}
