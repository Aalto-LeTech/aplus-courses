package fi.aalto.cs.apluscourses.model;

import static fi.aalto.cs.apluscourses.model.Component.LOADED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.utils.Version;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class ModuleTest {

  private static final ModelFactory MODEL_FACTORY = new ModelExtensions.TestModelFactory() {
  };

  @Test
  public void testCreateModule() throws MalformedURLException {
    String name = "Awesome module";
    URL url = new URL("https://example.com");
    Version version = new Version(2, 0);
    Version localVersion = new Version(1, 0);
    String changelog = "there has been some changes";
    ZonedDateTime downloadedAt = ZonedDateTime.now();

    Module module =
        new ModelExtensions.TestModule(name, url, version, localVersion, changelog, downloadedAt);
    ModuleMetadata metadata = module.getMetadata();

    assertEquals("The name of the module should be the same as that given to the constructor",
        name, module.getName());
    assertEquals("The URL of the module should be the same as that given to the constructor",
        url, module.getUrl());
    assertEquals("The id of the module metadata should be the local id given to the constructor",
        localVersion, metadata.getVersion());
    assertEquals("The metadata should have the correct time stamp",
        downloadedAt, metadata.getDownloadedAt());
  }

  @Test
  public void testCreateModuleFromJsonObject() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\",\"url\":\"https://aalto.fi\","
        + "\"changelog\":\"changes\",\"version\":\"2.5\"}");
    Module module = Module.fromJsonObject(jsonObject, MODEL_FACTORY);
    assertEquals("The name of the module should be the same as that in the JSON object",
        "Name", module.getName());
    assertEquals("The URL of the module should be the same as that in the JSON object",
        new URL("https://aalto.fi"), module.getUrl());
    assertEquals("The version of the module should be the same as that in the JSON object",
        new Version(2, 5), module.getVersion());
    assertEquals("The changelog of the module should be the same as that in the JSON object",
        "changes", module.getChangelog());
  }

  @Test
  public void testCreateModuleFromJsonObjectWithoutVersion() throws MalformedURLException {
    JSONObject jsonObject
        = new JSONObject("{\"name\":\"Name\",\"url\":\"https://example.org\"}");
    Module module = Module.fromJsonObject(jsonObject, MODEL_FACTORY);
    assertEquals("The module version should default to 1.0", new Version(1, 0),
        module.getVersion());
  }

  @Test
  public void testIsMajorUpdate() throws MalformedURLException {
    var url = new URL("https://example.com");
    var downloadedAt = ZonedDateTime.now();
    Module moduleMajor = new ModelExtensions.TestModule("name", url, new Version(2, 1),
        new Version(1, 0), "", downloadedAt);
    Module moduleMinor = new ModelExtensions.TestModule("name", url, new Version(1, 1),
        new Version(1, 0), "", downloadedAt);
    moduleMajor.stateMonitor.set(LOADED);
    moduleMinor.stateMonitor.set(LOADED);
    assertTrue("The module has a major update", moduleMajor.isMajorUpdate());
    assertFalse("The module doesn't have a major update", moduleMinor.isMajorUpdate());
  }

  @Test(expected = JSONException.class)
  public void testCreateModuleFromJsonObjectMissingName() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"url\":\"https://example.org\"}");
    Module.fromJsonObject(jsonObject, MODEL_FACTORY);
  }

  @Test(expected = JSONException.class)
  public void testCreateModuleFromJsonObjectMissingUrl() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\"}");
    Module.fromJsonObject(jsonObject, MODEL_FACTORY);
  }

  @Test(expected = MalformedURLException.class)
  public void testCreateModuleFromJsonObjectWithMalformedUrl() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\",\"url\":\"\"}");
    Module.fromJsonObject(jsonObject, MODEL_FACTORY);
  }
}
