package fi.aalto.cs.apluscourses.presentation.module;

import static fi.aalto.cs.apluscourses.model.Component.LOADED;

import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.Version;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModuleListElementViewModelTest {

  @Test
  void testStateChanged() {
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
    Assertions.assertTrue(isOnChangedCalled.get());
    Assertions.assertNotNull(moduleViewModel); // prevent weak reference from expiring
  }

  @Test
  void testNameAndUrl() throws MalformedURLException {
    String name = "Wanda";
    String url = "https://example.com/wanda";
    Module module = new ModelExtensions.TestModule(
        name, new URL(url), new Version(1, 0), null, "", null);
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module);
    Assertions.assertEquals(name, moduleViewModel.getName(), "getName() should return module's name");
    Assertions.assertEquals(url, moduleViewModel.getUrl(), "getUrl() should return module's URL");
  }

  @Test
  void testModuleTooltip() throws MalformedURLException {
    URL url = new URL("https://example.com/wanda");
    Module moduleAvailable = new ModelExtensions.TestModule(
        "", url, new Version(1, 0), null, "", null);
    ModuleListElementViewModel moduleViewModelAvailable =
        new ModuleListElementViewModel(moduleAvailable);
    Assertions.assertTrue(moduleViewModelAvailable.getTooltip().contains("Available"),
        "The tooltip for a non-downloaded module should contain Available");

    Module moduleInstalled = new ModelExtensions.TestModule(
        "", url, new Version(1, 0), null, "don't show this", ZonedDateTime.now());
    ModuleListElementViewModel moduleViewModelInstalled =
        new ModuleListElementViewModel(moduleInstalled);
    Assertions.assertTrue(moduleViewModelInstalled.getTooltip().contains("Installed"),
        "The tooltip for a downloaded module should contain Installed");

    Module moduleChangelog = new ModelExtensions.TestModule(
        "", url, new Version(1, 0), new Version(0, 1), "changes", ZonedDateTime.now());
    ModuleListElementViewModel moduleViewModelChangelog =
        new ModuleListElementViewModel(moduleChangelog);

    moduleChangelog.stateMonitor.set(LOADED);

    Assertions.assertTrue(moduleViewModelChangelog.getTooltip().contains("What's new"),
        "The tooltip for a module with a changelog should contain What's new");
    Assertions.assertTrue(moduleViewModelChangelog.getTooltip().contains("changes"),
        "The tooltip for a module with a changelog should contain the changelog");
  }

  @Test
  void testStatus() {
    Module module = new ModelExtensions.TestModule("testStatusModule");
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module);

    Assertions.assertEquals("Unknown", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.NOT_INSTALLED);
    Assertions.assertEquals("Double-click to install", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.FETCHING);
    Assertions.assertEquals("Downloading...", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.FETCHED);
    Assertions.assertEquals("Double-click to install", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.LOADING);
    Assertions.assertEquals("Installing...", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(LOADED);
    Assertions.assertEquals("Installed; dependencies unknown", moduleViewModel.getStatus());
    Assertions.assertTrue(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.UNINSTALLING);
    Assertions.assertEquals("Removing...", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.ERROR);
    Assertions.assertEquals("Error", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(Component.UNINSTALLED);
    Assertions.assertEquals("Removed", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());

    module.stateMonitor.set(LOADED);

    module.dependencyStateMonitor.set(Component.DEP_WAITING);
    Assertions.assertEquals("Waiting for dependencies...", moduleViewModel.getStatus());
    Assertions.assertTrue(moduleViewModel.isBoldface());

    module.dependencyStateMonitor.set(Component.DEP_LOADED);
    Assertions.assertEquals("Installed", moduleViewModel.getStatus());
    Assertions.assertTrue(moduleViewModel.isBoldface());

    module.dependencyStateMonitor.set(Component.DEP_ERROR);
    Assertions.assertEquals("Error in dependencies", moduleViewModel.getStatus());
    Assertions.assertFalse(moduleViewModel.isBoldface());
  }

  @Test
  void testGetSearchableString() {
    String name = "Wanda";
    Module module = new ModelExtensions.TestModule(name);
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module);

    Assertions.assertEquals(name, moduleViewModel.getSearchableString());
  }
}
