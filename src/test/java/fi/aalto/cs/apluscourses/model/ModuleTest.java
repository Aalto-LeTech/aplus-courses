package fi.aalto.cs.apluscourses.model;

import static fi.aalto.cs.apluscourses.model.Component.LOADED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fi.aalto.cs.apluscourses.utils.Version;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModuleTest {

  private static final ModelFactory MODEL_FACTORY = new ModelExtensions.TestModelFactory() {
  };

  @Test
  void testCreateModule() throws MalformedURLException {
    String name = "Awesome module";
    URL url = new URL("https://example.com");
    Version version = new Version(2, 0);
    Version localVersion = new Version(1, 0);
    String changelog = "there has been some changes";
    ZonedDateTime downloadedAt = ZonedDateTime.now();

    Module module =
        new ModelExtensions.TestModule(name, url, version, localVersion, changelog, downloadedAt);
    ModuleMetadata metadata = module.getMetadata();

    Assertions.assertEquals(name, module.getName(),
        "The name of the module should be the same as that given to the constructor");
    Assertions.assertEquals(url, module.getUrl(),
        "The URL of the module should be the same as that given to the constructor");
    Assertions.assertEquals(localVersion, metadata.getVersion(),
        "The id of the module metadata should be the local id given to the constructor");
    Assertions.assertEquals(downloadedAt, metadata.getDownloadedAt(),
        "The metadata should have the correct time stamp");
  }

  @Test
  void testCreateModuleFromJsonObject() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\",\"url\":\"https://aalto.fi\","
        + "\"changelog\":\"changes\",\"version\":\"2.5\"}");
    Module module = Module.fromJsonObject(jsonObject, MODEL_FACTORY);
    Assertions.assertEquals("Name", module.getName(),
        "The name of the module should be the same as that in the JSON object");
    Assertions.assertEquals(new URL("https://aalto.fi"), module.getUrl(),
        "The URL of the module should be the same as that in the JSON object");
    Assertions.assertEquals(new Version(2, 5), module.getVersion(),
        "The version of the module should be the same as that in the JSON object");
    Assertions.assertEquals("changes", module.getChangelog(),
        "The changelog of the module should be the same as that in the JSON object");
  }

  @Test
  void testCreateModuleFromJsonObjectWithoutVersion() throws MalformedURLException {
    JSONObject jsonObject
        = new JSONObject("{\"name\":\"Name\",\"url\":\"https://example.org\"}");
    Module module = Module.fromJsonObject(jsonObject, MODEL_FACTORY);
    Assertions.assertEquals(new Version(1, 0), module.getVersion(), "The module version should default to 1.0");
  }

  @Test
  void testIsMajorUpdate() throws MalformedURLException {
    var url = new URL("https://example.com");
    var downloadedAt = ZonedDateTime.now();
    Module moduleMajor = new ModelExtensions.TestModule("name", url, new Version(2, 1),
        new Version(1, 0), "", downloadedAt);
    Module moduleMinor = new ModelExtensions.TestModule("name", url, new Version(1, 1),
        new Version(1, 0), "", downloadedAt);
    moduleMajor.stateMonitor.set(LOADED);
    moduleMinor.stateMonitor.set(LOADED);
    Assertions.assertTrue(moduleMajor.isMajorUpdate(), "The module has a major update");
    Assertions.assertFalse(moduleMinor.isMajorUpdate(), "The module doesn't have a major update");
  }

  @Test
  void testCreateModuleFromJsonObjectMissingName() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"url\":\"https://example.org\"}");
    assertThrows(JSONException.class, () ->
        Module.fromJsonObject(jsonObject, MODEL_FACTORY));
  }

  @Test
  void testCreateModuleFromJsonObjectMissingUrl() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\"}");
    assertThrows(JSONException.class, () ->
        Module.fromJsonObject(jsonObject, MODEL_FACTORY));
  }

  @Test
  void testCreateModuleFromJsonObjectWithMalformedUrl() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\",\"url\":\"\"}");
    assertThrows(MalformedURLException.class, () ->
        Module.fromJsonObject(jsonObject, MODEL_FACTORY));
  }
}
