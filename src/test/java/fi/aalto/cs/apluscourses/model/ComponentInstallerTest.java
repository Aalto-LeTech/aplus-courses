package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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

public class ComponentInstallerTest {

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

    ComponentInstaller.Dialogs dialogs = mock(ComponentInstaller.Dialogs.class);

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(), dialogs);

    installer.install(module);

    verify(dialogs, never()).shouldOverwrite(any());

    InOrder order = inOrder(module);
    order.verify(module).fetch();
    order.verify(module).load();

    assertEquals("Module should be in LOADED state, after the installation has ended.",
        Component.LOADED, module.stateMonitor.get());
    assertEquals("Module should be in DEP_LOADED state, after the installation has ended.",
        Component.DEP_LOADED, module.dependencyStateMonitor.get());
  }

  @Test
  public void testInstallDependencies()
      throws IOException, ComponentLoadException {
    Module module1 = spy(new ModelExtensions.TestModule("dependentModule") {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
        assertEquals("Module should be in LOADED state when computeDependencies() is called.",
            Component.LOADED, stateMonitor.get());
        List<String> dependencies = new ArrayList<>();
        dependencies.add("firstDep");
        dependencies.add("secondDep");
        return dependencies;
      }
    });
    Module firstDep = spy(new ModelExtensions.TestModule("firstDep"));
    Module secondDep = spy(new ModelExtensions.TestModule("secondDep"));

    ComponentSource componentSource = spy(new ModelExtensions.TestComponentSource());
    when(componentSource.getComponentIfExists(module1.getName())).thenReturn(module1);
    when(componentSource.getComponentIfExists(firstDep.getName())).thenReturn(firstDep);
    when(componentSource.getComponentIfExists(secondDep.getName())).thenReturn(secondDep);

    ComponentInstaller installer =
        new ComponentInstallerImpl<>(componentSource, new SimpleAsyncTaskManager(),
            mock(ComponentInstaller.Dialogs.class));

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

    assertEquals("Dependent module should be in LOADED state, after the installation has ended.",
        Component.LOADED, module1.stateMonitor.get());
    assertEquals("Dependent module should be in DEP_LOADED state, after the installation.",
        Component.DEP_LOADED, module1.dependencyStateMonitor.get());

    assertEquals("1st dependency should be in LOADED state.",
        Component.LOADED, firstDep.stateMonitor.get());

    assertEquals("2nd dependency should be in LOADED state.",
        Component.LOADED, secondDep.stateMonitor.get());
  }

  @Test
  public void testInstallMany()
      throws IOException, ComponentLoadException {
    Module module1 = spy(new ModelExtensions.TestModule("module1"));
    Module module2 = spy(new ModelExtensions.TestModule("module2"));

    ComponentSource componentSource = spy(new ModelExtensions.TestComponentSource());
    when(componentSource.getComponentIfExists(module1.getName())).thenReturn(module1);
    when(componentSource.getComponentIfExists(module2.getName())).thenReturn(module2);

    ComponentInstaller installer =
        new ComponentInstallerImpl<>(componentSource, new SimpleAsyncTaskManager(),
            mock(ComponentInstaller.Dialogs.class));

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

    assertEquals("Module 1 should be in LOADED state, after the installation has ended.",
        Component.LOADED, module1.stateMonitor.get());

    assertEquals("Module 2 should be in LOADED state, after the installation has ended.",
        Component.LOADED, module2.stateMonitor.get());
  }

  @Test
  public void testInstallFetchFails() throws ComponentLoadException {
    Module module = spy(new ModelExtensions.TestModule("fetchFailModule") {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });


    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(),
        mock(ComponentInstaller.Dialogs.class));

    installer.install(module);

    verify(module, never()).getDependencies();
    verify(module, never()).load();

    assertTrue("Fetch-fail-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallLoadFails() {
    String moduleName = "loadFailModule";
    Module module = spy(new ModelExtensions.TestModule(moduleName) {
      @Override
      public void load() throws ComponentLoadException {
        throw new ComponentLoadException(moduleName, null);
      }
    });

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(),
        mock(ComponentInstaller.Dialogs.class));

    installer.install(module);

    assertTrue("Load-fail-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallUnknownDependency() {
    String nonExistentModuleName = "nonExistentModule";

    Module module = spy(new ModelExtensions.TestModule("unknownDepModule") {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
        return Collections.singletonList(nonExistentModuleName);
      }
    });

    ComponentSource componentSource = new ModelExtensions.TestComponentSource();

    ComponentInstaller installer =
        new ComponentInstallerImpl<>(componentSource, new SimpleAsyncTaskManager(),
            mock(ComponentInstaller.Dialogs.class));

    installer.install(module);

    assertTrue("Unknown-dep-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallDependencyFails() {
    Module dependentModule = spy(new ModelExtensions.TestModule("dependentModule") {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
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

    ComponentSource componentSource = spy(new ModelExtensions.TestComponentSource());
    when(componentSource.getComponentIfExists(dependentModule.getName()))
        .thenReturn(dependentModule);
    when(componentSource.getComponentIfExists(otherModule.getName())).thenReturn(otherModule);
    when(componentSource.getComponentIfExists(failingDep.getName())).thenReturn(failingDep);

    ComponentInstaller installer =
        new ComponentInstallerImpl<>(componentSource, new SimpleAsyncTaskManager(),
            mock(ComponentInstaller.Dialogs.class));

    List<Component> modules = new ArrayList<>();
    modules.add(dependentModule);
    modules.add(otherModule);

    installer.install(modules);

    assertEquals("Dependent module should be in LOADED state, after the installation has ended.",
        Component.LOADED, dependentModule.stateMonitor.get());
    assertEquals("Other module should be in LOADED state, after the installation has ended.",
        Component.LOADED, otherModule.stateMonitor.get());
    assertEquals("Dependent module should be in DEP_ERROR state, after the installation.",
        Component.DEP_ERROR, dependentModule.dependencyStateMonitor.get());
    assertEquals("Other module should be in DEP_LOADED state, after the installation.",
        Component.DEP_LOADED, otherModule.dependencyStateMonitor.get());
    assertTrue("Failing dependency should be in an error state, after the installation has ended.",
        failingDep.hasError());
  }

  @Test
  public void testInstallCircularDependency() {
    Module moduleA = spy(new ModelExtensions.TestModule("moduleA") {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
        return Collections.singletonList("moduleB");
      }
    });
    Module moduleB = spy(new ModelExtensions.TestModule("moduleB") {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
        return Collections.singletonList("moduleA");
      }
    });

    ComponentSource componentSource = spy(new ModelExtensions.TestComponentSource());
    when(componentSource.getComponentIfExists(moduleA.getName())).thenReturn(moduleA);
    when(componentSource.getComponentIfExists(moduleB.getName())).thenReturn(moduleB);

    ComponentInstaller installer =
        new ComponentInstallerImpl<>(componentSource, new SimpleAsyncTaskManager(),
            mock(ComponentInstaller.Dialogs.class));

    List<Component> modules = new ArrayList<>();
    modules.add(moduleA);
    modules.add(moduleB);

    installer.install(modules);

    assertEquals("Module A should be in LOADED state, after the installation has ended.",
        Component.LOADED, moduleA.stateMonitor.get());
    assertEquals("Module B should be in LOADED state, after the installation has ended.",
        Component.LOADED, moduleB.stateMonitor.get());
    assertEquals("Module A should be in DEP_LOADED state, after the installation has ended.",
        Component.DEP_LOADED, moduleA.dependencyStateMonitor.get());
    assertEquals("Module B should be in DEP_LOADED state, after the installation has ended.",
        Component.DEP_LOADED, moduleB.dependencyStateMonitor.get());
  }

  @Test
  public void testUpdate() throws ComponentLoadException, IOException {
    Component component = spy(new ModelExtensions.TestComponent());
    doReturn(true).when(component).isUpdatable();
    doReturn(true).when(component).hasLocalChanges();

    component.stateMonitor.set(Component.LOADED);

    ComponentInstaller.Dialogs dialogs = mock(ComponentInstaller.Dialogs.class);
    doReturn(true).when(dialogs).shouldOverwrite(component);

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(), dialogs);
    installer.install(component);

    verify(component).fetch();
    verify(component).load();

    assertEquals(Component.LOADED, component.stateMonitor.get());
  }

  @Test
  public void testUpdateCancelled() throws ComponentLoadException, IOException {
    Component component = spy(new ModelExtensions.TestComponent());
    doReturn(true).when(component).isUpdatable();
    doReturn(true).when(component).hasLocalChanges();
    doReturn(Component.LOADED).when(component).resolveStateInternal();

    component.stateMonitor.set(Component.LOADED);

    ComponentInstaller.Dialogs dialogs = mock(ComponentInstaller.Dialogs.class);
    doReturn(false).when(dialogs).shouldOverwrite(component);

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(), dialogs);
    installer.install(component);

    verify(component, never()).fetch();
    verify(component, never()).load();

    assertEquals(Component.LOADED, component.stateMonitor.get());
  }

  @Test
  public void testUpdateWithoutAsking() throws ComponentLoadException, IOException {
    Component component = spy(new ModelExtensions.TestComponent());
    doReturn(true).when(component).isUpdatable();
    doReturn(false).when(component).hasLocalChanges();

    component.stateMonitor.set(Component.LOADED);

    ComponentInstaller.Dialogs dialogs = mock(ComponentInstaller.Dialogs.class);

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(), dialogs);
    installer.install(component);

    verify(dialogs, never()).shouldOverwrite(any());

    verify(component).fetch();
    verify(component).load();

    assertEquals(Component.LOADED, component.stateMonitor.get());
  }
}
