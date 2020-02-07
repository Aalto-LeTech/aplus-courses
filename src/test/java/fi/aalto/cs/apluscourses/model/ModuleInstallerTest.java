package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

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
    Module module = spy(new Module("testModule", new URL("https://example.com")) {
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
    Module module1 = spy(new Module("module1", new URL("https://example.com/module1")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        assertEquals(Module.FETCHED, stateMonitor.get());
        List<String> dependencies = new ArrayList<>();
        dependencies.add("module2");
        dependencies.add("module3");
        return dependencies;
      }
    });
    Module module2 = spy(new Module("module2", new URL("https://example.com/module2")));
    Module module3 = spy(new Module("module3", new URL("https://example.com/module3")));

    Map<String, Module> moduleMap = new HashMap<>();
    moduleMap.put("module1", module1);
    moduleMap.put("module2", module2);
    moduleMap.put("module3", module3);

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleMap::get, new SimpleAsyncTaskManager());

    installer.install(module1);

    InOrder order1 = inOrder(module1);
    order1.verify(module1, times(1)).fetch();
    order1.verify(module1, times(1)).load();

    InOrder order2 = inOrder(module2);
    order2.verify(module2, times(1)).fetch();
    order2.verify(module2, times(1)).load();

    InOrder order3 = inOrder(module3);
    order3.verify(module3, times(1)).fetch();
    order3.verify(module3, times(1)).load();

    assertEquals("Module 1 should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module1.stateMonitor.get());

    assertEquals("Module 2 should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module2.stateMonitor.get());

    assertEquals("Module 3 should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module3.stateMonitor.get());
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
  public void testInstallFetchFails() throws MalformedURLException {
    Module module = spy(new Module("testModule", new URL("https://example.com")) {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleName -> null, new SimpleAsyncTaskManager());

    installer.install(module);

    assertTrue("Module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallLoadFails() throws MalformedURLException {
    Module module = spy(new Module("testModule", new URL("https://example.com")) {
      @Override
      public void load() throws ModuleLoadException {
        throw new ModuleLoadException(this, null);
      }
    });

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleName -> null, new SimpleAsyncTaskManager());

    installer.install(module);

    assertTrue("Module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallUnknownDependency() throws MalformedURLException {
    Module module = spy(new Module("testModule", new URL("https://example.com")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("nonExistentModule");
      }
    });

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleName -> null, new SimpleAsyncTaskManager());

    installer.install(module);

    assertTrue("Module should be in an error state, after the installation has ended.",
        module.hasError());
  }

  @Test
  public void testInstallDependencyFails() throws MalformedURLException {
    Module module1 = spy(new Module("module1", new URL("https://example.com/module1")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("module3");
      }
    });
    Module module2 = spy(new Module("module2", new URL("https://example.com/module2")));
    Module module3 = spy(new Module("module3", new URL("https://example.com/module3")) {
      @Override
      public void fetch() throws IOException {
        throw new IOException();
      }
    });

    Map<String, Module> moduleMap = new HashMap<>();
    moduleMap.put("module1", module1);
    moduleMap.put("module2", module2);
    moduleMap.put("module3", module3);

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleMap::get, new SimpleAsyncTaskManager());

    List<Module> modules = new ArrayList<>();
    modules.add(module1);
    modules.add(module2);

    installer.install(modules);

    assertTrue("Module 1 should be in an error state, after the installation has ended.",
        module1.hasError());
    assertEquals("Module 2 should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module2.stateMonitor.get());
    assertTrue("Module 3 should be in an error state, after the installation has ended.",
        module3.hasError());
  }

  @Test
  public void testInstallCircularDependency() throws MalformedURLException {
    Module module1 = spy(new Module("module1", new URL("https://example.com/module1")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("module2");
      }
    });
    Module module2 = spy(new Module("module2", new URL("https://example.com/module2")) {
      @NotNull
      @Override
      public List<String> getDependencies() {
        return Collections.singletonList("module1");
      }
    });

    Map<String, Module> moduleMap = new HashMap<>();
    moduleMap.put("module1", module1);
    moduleMap.put("module2", module2);

    ModuleInstaller<CompletableFuture<Void>> installer =
        new ModuleInstaller<>(moduleMap::get, new SimpleAsyncTaskManager());

    List<Module> modules = new ArrayList<>();
    modules.add(module1);
    modules.add(module2);

    installer.install(modules);

    assertEquals("Module 1 should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module2.stateMonitor.get());
    assertEquals("Module 2 should be in INSTALLED state, after the installation has ended.",
        Module.INSTALLED, module2.stateMonitor.get());
  }

}
