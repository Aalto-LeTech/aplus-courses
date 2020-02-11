package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fi.aalto.cs.apluscourses.utils.SimpleAsyncTaskManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.InOrder;

public class ModuleInstallerTest {

  @Test
  public void testInstall() throws IOException, ModuleLoadException {
    Module module = spy(new Module("someModule", new URL("https://example.com")) {
      @Override
      public void fetch() {
        assertEquals("When fetch() is called, module should be in FETCHING state.",
            Module.FETCHING, stateMonitor.get());
      }

      @Override
      public void load() {
        assertEquals("When load() is called, module should be in LOADING state.",
            Module.LOADING, stateMonitor.get());
      }
    });

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleName -> null, new SimpleAsyncTaskManager());

    installer.install(module);

    InOrder order = inOrder(module);
    order.verify(module, times(1)).fetch();
    order.verify(module, times(1)).load();

    assertEquals("Module should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module.stateMonitor.get());
  }

  @Test
  public void testInstallDependencies() throws IOException, ModuleLoadException {
    Module module1 = spy(new Module("dependentModule", new URL("https://example.com/1")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        assertThat("Module should be at least in FETCHED state when getDependencies() is called.",
            stateMonitor.get(), greaterThanOrEqualTo(Module.FETCHED));
        List<String> dependencies = new ArrayList<>();
        dependencies.add("firstDep");
        dependencies.add("secondDep");
        return dependencies;
      }
    });
    Module firstDep = spy(new Module("firstDep", new URL("https://example.com/2")));
    Module secondDep = spy(new Module("secondDep", new URL("https://example.com/3")));

    Map<String, Module> moduleMap = new HashMap<>();
    moduleMap.put("dependentModule", module1);
    moduleMap.put("firstDep", firstDep);
    moduleMap.put("secondDep", secondDep);

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleMap::get, new SimpleAsyncTaskManager());

    installer.install(module1);

    InOrder order1 = inOrder(module1);
    order1.verify(module1, times(1)).fetch();
    order1.verify(module1, times(1)).load();

    InOrder order2 = inOrder(firstDep);
    order2.verify(firstDep, times(1)).fetch();
    order2.verify(firstDep, times(1)).load();

    InOrder order3 = inOrder(secondDep);
    order3.verify(secondDep, times(1)).fetch();
    order3.verify(secondDep, times(1)).load();

    assertEquals("Dependent module should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module1.stateMonitor.get());

    assertEquals("1st dependency should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, firstDep.stateMonitor.get());

    assertEquals("2nd dependency should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, secondDep.stateMonitor.get());
  }

  @Test
  public void testInstallMany() throws IOException, ModuleLoadException {
    Module module1 = spy(new Module("module1", new URL("https://example.com/module1")));
    Module module2 = spy(new Module("module2", new URL("https://example.com/module2")));

    Map<String, Module> moduleMap = new HashMap<>();
    moduleMap.put("module1", module1);
    moduleMap.put("module2", module2);

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleMap::get, new SimpleAsyncTaskManager());

    List<Module> modules = new ArrayList<>();
    modules.add(module1);
    modules.add(module2);

    installer.install(modules);

    InOrder order1 = inOrder(module1);
    order1.verify(module1, times(1)).fetch();
    order1.verify(module1, times(1)).load();
    InOrder order2 = inOrder(module2);
    order2.verify(module2, times(1)).fetch();
    order2.verify(module2, times(1)).load();

    assertEquals("Module 1 should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module1.stateMonitor.get());

    assertEquals("Module 2 should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module2.stateMonitor.get());
  }

  @Test
  public void testInstallFetchFails() throws MalformedURLException, ModuleLoadException {
    Module module = spy(new Module("fetchFailModule", new URL("https://example.com")) {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleName -> null, new SimpleAsyncTaskManager());

    installer.install(module);

    verify(module, never()).getDependencies();
    verify(module, never()).load();

    assertTrue("Fetch-fail-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallLoadFails() throws MalformedURLException {
    Module module = spy(new Module("loadFailModule", new URL("https://example.com")) {
      @Override
      public void load() throws ModuleLoadException {
        throw new ModuleLoadException(this, null);
      }
    });

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleName -> null, new SimpleAsyncTaskManager());

    installer.install(module);

    assertTrue("Load-fail-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallUnknownDependency() throws MalformedURLException, ModuleLoadException {
    Module module = spy(new Module("unknownDepModule", new URL("https://example.com")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("nonExistentModule");
      }
    });

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleName -> null, new SimpleAsyncTaskManager());

    installer.install(module);

    verify(module, never()).load();

    assertTrue("Unknown-dep-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallGetDependenciesFail() throws MalformedURLException, ModuleLoadException {
    Module module = spy(new Module("failingDepModule", new URL("https://example.com")) {
      @NotNull
      @Override
      public List<String> getDependencies() throws ModuleLoadException {
        throw new ModuleLoadException(this, null);
      }
    });

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleName -> null, new SimpleAsyncTaskManager());

    installer.install(module);

    verify(module, never()).load();

    assertTrue("Unknown-dep-module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallDependencyFails() throws MalformedURLException {
    Module dependentModule = spy(new Module("dependentModule", new URL("https://example.com/1")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("failingDependency");
      }
    });
    Module otherModule = spy(new Module("otherModule", new URL("https://example.com/2")));
    Module failingDependency = spy(new Module("failingDependency", new URL("https://example.com/3")) {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });

    Map<String, Module> moduleMap = new HashMap<>();
    moduleMap.put("dependentModule", dependentModule);
    moduleMap.put("otherModule", otherModule);
    moduleMap.put("failingDependency", failingDependency);

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleMap::get, new SimpleAsyncTaskManager());

    List<Module> modules = new ArrayList<>();
    modules.add(dependentModule);
    modules.add(otherModule);

    installer.install(modules);

    assertTrue("Dependent module should be in an error state, after the installation has ended.",
        dependentModule.hasError());
    assertEquals("Other module should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, otherModule.stateMonitor.get());
    assertTrue("Failing dependency should be in an error state, after the installation has ended.",
        failingDependency.hasError());
  }

  @Test
  public void testInstallCircularDependency() throws MalformedURLException {
    Module moduleA = spy(new Module("moduleA", new URL("https://example.com/1")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("moduleB");
      }
    });
    Module moduleB = spy(new Module("moduleB", new URL("https://example.com/2")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("moduleA");
      }
    });

    Map<String, Module> moduleMap = new HashMap<>();
    moduleMap.put("moduleA", moduleA);
    moduleMap.put("moduleB", moduleB);

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleMap::get, new SimpleAsyncTaskManager());

    List<Module> modules = new ArrayList<>();
    modules.add(moduleA);
    modules.add(moduleB);

    installer.install(modules);

    assertEquals("Module A should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, moduleA.stateMonitor.get());
    assertEquals("Module B should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, moduleB.stateMonitor.get());
  }
}
