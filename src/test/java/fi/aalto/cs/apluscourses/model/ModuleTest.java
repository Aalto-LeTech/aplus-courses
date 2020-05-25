package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class ModuleTest {

  private static final ModelFactory MODEL_FACTORY = new ModelExtensions.TestModelFactory() {};

  @Test
  public void testCreateModule() throws MalformedURLException {
    String name = "Awesome module";
    URL url = new URL("https://example.com");
    String id = "cool id";
    Module module = new ModelExtensions.TestModule(name, url, id);
    assertEquals("The name of the module should be the same as that given to the constructor",
        name, module.getName());
    assertEquals("The URL of the module should be the same as that given to the constructor",
        url, module.getUrl());
    assertEquals("The id of the module should be the same as that given to the constructor",
        id, module.getVersionId());
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
