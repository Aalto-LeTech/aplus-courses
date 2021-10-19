package fi.aalto.cs.apluscourses.presentation.module;

import static fi.aalto.cs.apluscourses.model.Component.LOADED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.Version;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

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
    Module module = new ModelExtensions.TestModule(
        name, new URL(url), new Version(1, 0), null, "", null);
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module);
    assertEquals("getName() should return module's name",
        name, moduleViewModel.getName());
    assertEquals("getUrl() should return module's URL",
        url, moduleViewModel.getUrl());
  }

  @Test
  public void testModuleTooltip() throws MalformedURLException {
    URL url = new URL("https://example.com/wanda");
    Module moduleAvailable = new ModelExtensions.TestModule(
        "", url, new Version(1, 0), null, "", null);
    ModuleListElementViewModel moduleViewModelAvailable =
        new ModuleListElementViewModel(moduleAvailable);
    assertTrue("The tooltip for a non-downloaded module should contain Available",
        moduleViewModelAvailable.getTooltip().contains("Available"));

    Module moduleInstalled = new ModelExtensions.TestModule(
            "", url, new Version(1, 0), null, "don't show this", ZonedDateTime.now());
    ModuleListElementViewModel moduleViewModelInstalled =
        new ModuleListElementViewModel(moduleInstalled);
    assertTrue("The tooltip for a downloaded module should contain Installed",
        moduleViewModelInstalled.getTooltip().contains("Installed"));

    Module moduleChangelog = new ModelExtensions.TestModule(
        "", url, new Version(1, 0), new Version(0, 1), "changes", ZonedDateTime.now());
    ModuleListElementViewModel moduleViewModelChangelog =
        new ModuleListElementViewModel(moduleChangelog);

    moduleChangelog.stateMonitor.set(LOADED);

    assertTrue("The tooltip for a module with a changelog should contain What's new",
        moduleViewModelChangelog.getTooltip().contains("What's new"));
    assertTrue("The tooltip for a module with a changelog should contain the changelog",
        moduleViewModelChangelog.getTooltip().contains("changes"));
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

    module.stateMonitor.set(LOADED);
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

    module.stateMonitor.set(LOADED);

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
