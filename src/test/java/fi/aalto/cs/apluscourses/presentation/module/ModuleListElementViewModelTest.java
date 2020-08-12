package fi.aalto.cs.apluscourses.presentation.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import java.awt.font.TextAttribute;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

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
  public void testStatus() {
    Module module = new ModelExtensions.TestModule("testStatusModule");
    ModuleListElementViewModel moduleViewModel = new ModuleListElementViewModel(module);

    float delta = 0.001f;

    assertEquals("Unknown", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.NOT_INSTALLED);
    assertEquals("Double-click to download", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.FETCHING);
    assertEquals("Downloading...", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.FETCHED);
    assertEquals("Double-click to install", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.LOADING);
    assertEquals("Installing...", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.LOADED);
    assertEquals("Installed; dependencies unknown", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_BOLD, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.UNINSTALLING);
    assertEquals("Removing...", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.ERROR);
    assertEquals("Error", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.UNINSTALLED);
    assertEquals("Removed", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);

    module.stateMonitor.set(Component.LOADED);

    module.dependencyStateMonitor.set(Component.DEP_WAITING);
    assertEquals("Waiting for dependencies...", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_BOLD, moduleViewModel.getFontWeight(), delta);

    module.dependencyStateMonitor.set(Component.DEP_LOADED);
    assertEquals("Installed", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_BOLD, moduleViewModel.getFontWeight(), delta);

    module.dependencyStateMonitor.set(Component.DEP_ERROR);
    assertEquals("Error in dependencies", moduleViewModel.getStatus());
    assertEquals(TextAttribute.WEIGHT_REGULAR, moduleViewModel.getFontWeight(), delta);
  }
}
