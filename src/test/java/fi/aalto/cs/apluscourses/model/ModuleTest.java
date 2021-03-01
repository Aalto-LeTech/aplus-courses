package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

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
    String id = "cool id";
    String localId = "meh id";
    ZonedDateTime downloadedAt = ZonedDateTime.now();

    Module module = new ModelExtensions.TestModule(name, url, id, localId, downloadedAt);
    ModuleMetadata metadata = module.getMetadata();

    assertEquals("The name of the module should be the same as that given to the constructor",
        name, module.getName());
    assertEquals("The URL of the module should be the same as that given to the constructor",
        url, module.getUrl());
    assertEquals("The id of the module metadata should be the local id given to the constructor",
        localId, metadata.getModuleId());
    assertEquals("The metadata should have the correct time stamp",
        downloadedAt, metadata.getDownloadedAt());
  }

  @Test
  public void testCreateModuleFromJsonObject() throws MalformedURLException {
    JSONObject jsonObject
        = new JSONObject("{\"name\":\"Name\",\"url\":\"https://aalto.fi\",\"id\":\"abc\"}");
    Module module = Module.fromJsonObject(jsonObject, MODEL_FACTORY);
    assertEquals("The name of the module should be the same as that in the JSON object",
        "Name", module.getName());
    assertEquals("The URL of the module should be the same as that in the JSON object",
        new URL("https://aalto.fi"), module.getUrl());
    assertEquals("The id of the module should be the same as that in the JSON object",
        "abc", module.getVersionId());
  }

  @Test
  public void testCreateModuleFromJsonObjectWithoutId() throws MalformedURLException {
    JSONObject jsonObject
        = new JSONObject("{\"name\":\"Name\",\"url\":\"https://example.org\"}");
    Module module = Module.fromJsonObject(jsonObject, MODEL_FACTORY);
    assertEquals("The module id should default to an empty string", "", module.getVersionId());
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
