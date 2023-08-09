package fi.aalto.cs.apluscourses.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.aalto.cs.apluscourses.utils.Callbacks;
import fi.aalto.cs.apluscourses.utils.async.SimpleAsyncTaskManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class ComponentInstallerTest {

  @Test
  void testInstall() throws IOException, ComponentLoadException {
    Module module = spy(new ModelExtensions.TestModule("someModule") {
      @Override
      public void fetch() {
        Assertions.assertEquals(Component.FETCHING, stateMonitor.get(),
            "When fetch() is called, module should be in FETCHING state.");
      }

      @Override
      public void load() {
        Assertions.assertEquals(Component.LOADING, stateMonitor.get(),
            "When load() is called, module should be in LOADING state.");
      }
    });

    ComponentInstaller.Dialogs dialogs = mock(ComponentInstaller.Dialogs.class);

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(), dialogs, new Callbacks());

    installer.install(module);

    verify(dialogs, never()).shouldOverwrite(any());

    InOrder order = inOrder(module);
    order.verify(module).fetch();
    order.verify(module).load();

    Assertions.assertEquals(Component.LOADED, module.stateMonitor.get(),
        "Module should be in LOADED state, after the installation has ended.");
    Assertions.assertEquals(Component.DEP_LOADED, module.dependencyStateMonitor.get(),
        "Module should be in DEP_LOADED state, after the installation has ended.");
  }

  @Test
  void testInstallDependencies()
      throws IOException, ComponentLoadException {
    Module module1 = spy(new ModelExtensions.TestModule("dependentModule") {
      @NotNull
      @Override
      protected List<String> computeDependencies() {
        Assertions.assertEquals(Component.LOADED, stateMonitor.get(),
            "Module should be in LOADED state when computeDependencies() is called.");
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
            mock(ComponentInstaller.Dialogs.class), new Callbacks());

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

    Assertions.assertEquals(Component.LOADED, module1.stateMonitor.get(),
        "Dependent module should be in LOADED state, after the installation has ended.");
    Assertions.assertEquals(Component.DEP_LOADED, module1.dependencyStateMonitor.get(),
        "Dependent module should be in DEP_LOADED state, after the installation.");

    Assertions.assertEquals(Component.LOADED, firstDep.stateMonitor.get(), "1st dependency should be in LOADED state.");

    Assertions.assertEquals(Component.LOADED, secondDep.stateMonitor.get(),
        "2nd dependency should be in LOADED state.");
  }

  @Test
  void testInstallMany()
      throws IOException, ComponentLoadException {
    Module module1 = spy(new ModelExtensions.TestModule("module1"));
    Module module2 = spy(new ModelExtensions.TestModule("module2"));

    ComponentSource componentSource = spy(new ModelExtensions.TestComponentSource());
    when(componentSource.getComponentIfExists(module1.getName())).thenReturn(module1);
    when(componentSource.getComponentIfExists(module2.getName())).thenReturn(module2);

    ComponentInstaller installer =
        new ComponentInstallerImpl<>(componentSource, new SimpleAsyncTaskManager(),
            mock(ComponentInstaller.Dialogs.class), new Callbacks());

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

    Assertions.assertEquals(Component.LOADED, module1.stateMonitor.get(),
        "Module 1 should be in LOADED state, after the installation has ended.");

    Assertions.assertEquals(Component.LOADED, module2.stateMonitor.get(),
        "Module 2 should be in LOADED state, after the installation has ended.");
  }

  @Test
  void testInstallFetchFails() throws ComponentLoadException {
    Module module = spy(new ModelExtensions.TestModule("fetchFailModule") {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });


    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(),
        mock(ComponentInstaller.Dialogs.class), new Callbacks());

    installer.install(module);

    verify(module, never()).getDependencies();
    verify(module, never()).load();

    Assertions.assertTrue(module.hasError(),
        "Fetch-fail-module should be in an error state, after the installation has ended.");
  }

  @Test
  void testInstallLoadFails() {
    String moduleName = "loadFailModule";
    Module module = spy(new ModelExtensions.TestModule(moduleName) {
      @Override
      public void load() throws ComponentLoadException {
        throw new ComponentLoadException(moduleName, null);
      }
    });

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(),
        mock(ComponentInstaller.Dialogs.class), new Callbacks());

    installer.install(module);

    Assertions.assertTrue(module.hasError(),
        "Load-fail-module should be in an error state, after the installation has ended.");
  }

  @Test
  void testInstallUnknownDependency() {
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
            mock(ComponentInstaller.Dialogs.class), new Callbacks());

    installer.install(module);

    Assertions.assertTrue(module.hasError(),
        "Unknown-dep-module should be in an error state, after the installation has ended.");
  }

  @Test
  void testInstallDependencyFails() {
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
            mock(ComponentInstaller.Dialogs.class), new Callbacks());

    List<Component> modules = new ArrayList<>();
    modules.add(dependentModule);
    modules.add(otherModule);

    installer.install(modules);

    Assertions.assertEquals(Component.LOADED, dependentModule.stateMonitor.get(),
        "Dependent module should be in LOADED state, after the installation has ended.");
    Assertions.assertEquals(Component.LOADED, otherModule.stateMonitor.get(),
        "Other module should be in LOADED state, after the installation has ended.");
    Assertions.assertEquals(Component.DEP_ERROR, dependentModule.dependencyStateMonitor.get(),
        "Dependent module should be in DEP_ERROR state, after the installation.");
    Assertions.assertEquals(Component.DEP_LOADED, otherModule.dependencyStateMonitor.get(),
        "Other module should be in DEP_LOADED state, after the installation.");
    Assertions.assertTrue(failingDep.hasError(),
        "Failing dependency should be in an error state, after the installation has ended.");
  }

  @Test
  void testInstallCircularDependency() {
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
            mock(ComponentInstaller.Dialogs.class), new Callbacks());

    List<Component> modules = new ArrayList<>();
    modules.add(moduleA);
    modules.add(moduleB);

    installer.install(modules);

    Assertions.assertEquals(Component.LOADED, moduleA.stateMonitor.get(),
        "Module A should be in LOADED state, after the installation has ended.");
    Assertions.assertEquals(Component.LOADED, moduleB.stateMonitor.get(),
        "Module B should be in LOADED state, after the installation has ended.");
    Assertions.assertEquals(Component.DEP_LOADED, moduleA.dependencyStateMonitor.get(),
        "Module A should be in DEP_LOADED state, after the installation has ended.");
    Assertions.assertEquals(Component.DEP_LOADED, moduleB.dependencyStateMonitor.get(),
        "Module B should be in DEP_LOADED state, after the installation has ended.");
  }

  @Test
  void testUpdate() throws ComponentLoadException, IOException {
    Component component = spy(new ModelExtensions.TestComponent());
    doReturn(true).when(component).isUpdatable();
    doReturn(true).when(component).hasLocalChanges();

    component.stateMonitor.set(Component.LOADED);

    ComponentInstaller.Dialogs dialogs = mock(ComponentInstaller.Dialogs.class);
    doReturn(true).when(dialogs).shouldOverwrite(component);

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(), dialogs, new Callbacks());
    installer.install(component);

    verify(component).fetch();
    verify(component).load();

    Assertions.assertEquals(Component.LOADED, component.stateMonitor.get());
  }

  @Test
  void testUpdateCancelled() throws ComponentLoadException, IOException {
    Component component = spy(new ModelExtensions.TestComponent());
    doReturn(true).when(component).isUpdatable();
    doReturn(true).when(component).hasLocalChanges();
    doReturn(Component.LOADED).when(component).resolveStateInternal();

    component.stateMonitor.set(Component.LOADED);

    ComponentInstaller.Dialogs dialogs = mock(ComponentInstaller.Dialogs.class);
    doReturn(false).when(dialogs).shouldOverwrite(component);

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(), dialogs, new Callbacks());
    installer.install(component);

    verify(component, never()).fetch();
    verify(component, never()).load();

    Assertions.assertEquals(Component.LOADED, component.stateMonitor.get());
  }

  @Test
  void testUpdateWithoutAsking() throws ComponentLoadException, IOException {
    Component component = spy(new ModelExtensions.TestComponent());
    doReturn(true).when(component).isUpdatable();
    doReturn(false).when(component).hasLocalChanges();

    component.stateMonitor.set(Component.LOADED);

    ComponentInstaller.Dialogs dialogs = mock(ComponentInstaller.Dialogs.class);

    ComponentInstaller installer = new ComponentInstallerImpl<>(
        new ModelExtensions.TestComponentSource(), new SimpleAsyncTaskManager(), dialogs, new Callbacks());
    installer.install(component);

    verify(dialogs, never()).shouldOverwrite(any());

    verify(component).fetch();
    verify(component).load();

    Assertions.assertEquals(Component.LOADED, component.stateMonitor.get());
  }
}
