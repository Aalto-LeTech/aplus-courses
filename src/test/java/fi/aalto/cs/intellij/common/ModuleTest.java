package fi.aalto.cs.intellij.common;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class ModuleTest {

  @Test
  public void testCreateModule() throws MalformedURLException {
    URL url = new URL("https://example.com");
    Module module = new Module("Awesome module", url);
    assertEquals("The name of the module should be the same as that given to the constructor",
        "Awesome module", module.getName());
    assertEquals("The URL of the module should be the same as that given to the constructor",
        url, module.getUrl());
  }

  @Test
  public void testCreateModuleFromJsonObject() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\",\"url\":\"https://aalto.fi\"}");
    Module module = Module.fromJsonObject(jsonObject);
    assertEquals("The name of the module should be the same as that in the JSON object",
        "Name", module.getName());
    assertEquals("The URL of the module should be the same as that in the JSON object",
        new URL("https://aalto.fi"), module.getUrl());
  }

  @Test(expected = JSONException.class)
  public void testCreateModuleFromJsonObjectMissingName() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"url\":\"https://example.org\"}");
    Module.fromJsonObject(jsonObject);
  }

  @Test(expected = JSONException.class)
  public void testCreateModuleFromJsonObjectMissingUrl() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\"}");
    Module.fromJsonObject(jsonObject);
  }

  @Test(expected = MalformedURLException.class)
  public void testCreateModuleFromJsonObjectWithMalformedUrl() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\",\"url\":\"\"}");
    Module.fromJsonObject(jsonObject);
  }

}
