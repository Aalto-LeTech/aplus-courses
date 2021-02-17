package fi.aalto.cs.apluscourses.presentation.module;

import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ModuleListElementViewModelTest {

  @Test
  public void testStateChanged() {
    AtomicBoolean isOnChangedCalled = new AtomicBoolean(false);
    Module module = new ModelExtensions.TestModule("testModule");
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module) {
      @Override
      public void onChanged() {
        // For some weird reason, Mockito's spy-verify could not recognize this method call.
        isOnChangedCalled.set(true);
      }
    };
    module.stateMonitor.set(Component.FETCHING);
    assertTrue(isOnChangedCalled.get());
    assertNotNull(moduleViewModel); // prevent weak reference from expiring
  }

  @Test
  public void testNameAndUrl() throws MalformedURLException {
    String name = "Wanda";
    String url = "https://example.com/wanda";
    Module module = new ModelExtensions.TestModule(name, new URL(url), "", null, null);
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module);
    assertEquals("getName() should return module's name",
        name, moduleViewModel.getName());
    assertEquals("getUrl() should return module's URL",
        url, moduleViewModel.getUrl());
  }

  @Test
  public void testModuleTooltip() throws MalformedURLException {
    URL url = new URL("https://example.com/wanda");
    Module moduleAvailable = new ModelExtensions.TestModule("", url, "", null, null);
    Module moduleInstalled = new ModelExtensions.TestModule(
            "", url, "", null, ZonedDateTime.now());
    ModuleListElementViewModel moduleViewModelAvailable =
            new ModuleListElementViewModel(moduleAvailable);
    ModuleListElementViewModel moduleViewModelInstalled =
            new ModuleListElementViewModel(moduleInstalled);

    assertTrue("The tooltip for a non-downloaded module should contain Available",
            moduleViewModelAvailable.getTooltip().contains("Available"));
    assertTrue("The tooltip for a downloaded module should contain Installed",
            moduleViewModelInstalled.getTooltip().contains("Installed"));
  }

  @Test
  public void testStatus() {
    Module module = new ModelExtensions.TestModule("testStatusModule");
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module);

    assertEquals("Unknown", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.NOT_INSTALLED);
    assertEquals("Double-click to install", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.FETCHING);
    assertEquals("Downloading...", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.FETCHED);
    assertEquals("Double-click to install", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.LOADING);
    assertEquals("Installing...", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.LOADED);
    assertEquals("Installed; dependencies unknown", moduleViewModel.getStatus());
    assertTrue(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.UNINSTALLING);
    assertEquals("Removing...", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.ERROR);
    assertEquals("Error", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.UNINSTALLED);
    assertEquals("Removed", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.LOADED);

    module.dependencyStateMonitor.set(Component.DEP_WAITING);
    assertEquals("Waiting for dependencies...", moduleViewModel.getStatus());
    assertTrue(moduleViewModel.isBoldface());

    module.dependencyStateMonitor.set(Component.DEP_LOADED);
    assertEquals("Installed", moduleViewModel.getStatus());
    assertTrue(moduleViewModel.isBoldface());

    module.dependencyStateMonitor.set(Component.DEP_ERROR);
    assertEquals("Error in dependencies", moduleViewModel.getStatus());
    assertFalse(moduleViewModel.isBoldface());
  }

  @Test
  public void testGetSearchableString() {
    String name = "Wanda";
    Module module = new ModelExtensions.TestModule(name);
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module);

    assertEquals(name, moduleViewModel.getSearchableString());
  }
}
