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
  public void testInstall() throws IOException, ModuleLoadException {
    Module module = spy(new ModelExtensions.TestModule("someModule") {
      @Override
      public void fetch() {
        assertEquals("When fetch() is called, module should be in FETCHING state.",
            Installable.FETCHING, stateMonitor.get());
      }

      @Override
      public void load() {
        assertEquals("When load() is called, module should be in LOADING state.",
            Installable.LOADING, stateMonitor.get());
      }
    });

    Installer installer =
        new InstallerImpl<>(mock(ModuleSource.class), new SimpleAsyncTaskManager());

    installer.install(module);

    InOrder order = inOrder(module);
    order.verify(module).fetch();
    order.verify(module).load();

    assertEquals("Module should be in INSTALLED state, after the installation has ended.",
        Installable.INSTALLED, module.stateMonitor.get());
  }

  @Test
  public void testInstallDependencies()
      throws IOException, ModuleLoadException, NoSuchModuleException {
    Module module1 = spy(new ModelExtensions.TestModule("dependentModule") {
      @NotNull
      @Override
      public List<String> getDependencies() {
        assertThat("Module should be at least in FETCHED state when getDependencies() is called.",
            stateMonitor.get(), greaterThanOrEqualTo(Installable.FETCHED));
        List<String> dependencies = new ArrayList<>();
        dependencies.add("firstDep");
        dependencies.add("secondDep");
        return dependencies;
      }
    });
    Module firstDep = spy(new ModelExtensions.TestModule("firstDep"));
    Module secondDep = spy(new ModelExtensions.TestModule("secondDep"));

    ModuleSource moduleSource = mock(ModuleSource.class);
    when(moduleSource.getModule(module1.getName())).thenReturn(module1);
    when(moduleSource.getModule(firstDep.getName())).thenReturn(firstDep);
    when(moduleSource.getModule(secondDep.getName())).thenReturn(secondDep);

    Installer installer =
        new InstallerImpl<>(moduleSource, new SimpleAsyncTaskManager());

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
        Installable.INSTALLED, module1.stateMonitor.get());

    assertThat("1st dependency should be in LOADED state or further.",
        firstDep.stateMonitor.get(), greaterThanOrEqualTo(Installable.LOADED));

    assertThat("2nd dependency should be in LOADED state or further.",
        secondDep.stateMonitor.get(), greaterThanOrEqualTo(Installable.LOADED));
  }

  @Test
  public void testInstallMany() throws IOException, ModuleLoadException, NoSuchModuleException {
    Module module1 = spy(new ModelExtensions.TestModule("module1"));
    Module module2 = spy(new ModelExtensions.TestModule("module2"));

    ModuleSource moduleSource = mock(ModuleSource.class);
    when(moduleSource.getModule(module1.getName())).thenReturn(module1);
    when(moduleSource.getModule(module2.getName())).thenReturn(module2);

    Installer installer =
        new InstallerImpl<>(moduleSource, new SimpleAsyncTaskManager());

    List<Module> modules = new ArrayList<>();
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
        Installable.INSTALLED, module1.stateMonitor.get());

    assertEquals("Module 2 should be in INSTALLED state, after the installation has ended.",
        Installable.INSTALLED, module2.stateMonitor.get());
  }

  @Test
  public void testInstallFetchFails() throws ModuleLoadException {
    Module module = spy(new ModelExtensions.TestModule("fetchFailModule") {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });

    Installer installer =
        new InstallerImpl<>(mock(ModuleSource.class), new SimpleAsyncTaskManager());

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
      public void load() throws ModuleLoadException {
        throw new ModuleLoadException(this, null);
      }
    });

    Installer installer =
        new InstallerImpl<>(mock(ModuleSource.class), new SimpleAsyncTaskManager());

    installer.install(module);

    assertTrue("Load-fail-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallUnknownDependency() throws ModuleLoadException {
    Module module = spy(new ModelExtensions.TestModule("unknownDepModule") {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("nonExistentModule");
      }
    });

    ModuleSource moduleSource = moduleName -> {
      throw new NoSuchModuleException(moduleName, null);
    };

    Installer installer =
        new InstallerImpl<>(moduleSource, new SimpleAsyncTaskManager());

    installer.install(module);

    verify(module, never()).load();

    assertTrue("Unknown-dep-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallGetDependenciesFail() throws ModuleLoadException {
    Module module = spy(new ModelExtensions.TestModule("failingDepModule") {
      @NotNull
      @Override
      public List<String> getDependencies() throws ModuleLoadException {
        throw new ModuleLoadException(this, null);
      }
    });

    Installer installer =
        new InstallerImpl<>(mock(ModuleSource.class), new SimpleAsyncTaskManager());

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

    ModuleSource moduleSource = mock(ModuleSource.class);
    when(moduleSource.getModule(dependentModule.getName())).thenReturn(dependentModule);
    when(moduleSource.getModule(otherModule.getName())).thenReturn(otherModule);
    when(moduleSource.getModule(failingDep.getName())).thenReturn(failingDep);

    Installer installer =
        new InstallerImpl<>(moduleSource, new SimpleAsyncTaskManager());

    List<Module> modules = new ArrayList<>();
    modules.add(dependentModule);
    modules.add(otherModule);

    installer.install(modules);

    assertEquals("Dependent module should be in INSTALLED state, after the installation has ended.",
        Installable.INSTALLED, dependentModule.stateMonitor.get());
    assertEquals("Other module should be in INSTALLED state, after the installation has ended.",
        Installable.INSTALLED, otherModule.stateMonitor.get());
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

    ModuleSource moduleSource = mock(ModuleSource.class);
    when(moduleSource.getModule(moduleA.getName())).thenReturn(moduleA);
    when(moduleSource.getModule(moduleB.getName())).thenReturn(moduleB);

    Installer installer =
        new InstallerImpl<>(moduleSource, new SimpleAsyncTaskManager());

    List<Module> modules = new ArrayList<>();
    modules.add(moduleA);
    modules.add(moduleB);

    installer.install(modules);

    assertEquals("Module A should be in INSTALLED state, after the installation has ended.",
        Installable.INSTALLED, moduleA.stateMonitor.get());
    assertEquals("Module B should be in INSTALLED state, after the installation has ended.",
        Installable.INSTALLED, moduleB.stateMonitor.get());
  }
}
