package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import fi.aalto.cs.apluscourses.utils.Event;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class ModuleTest {

  private static final CourseFactory COURSE_FACTORY = new CourseFactory() {};

  @Test
  public void testCreateModule() throws MalformedURLException {
    String name = "Awesome module";
    URL url = new URL("https://example.com");
    Module module = new Module(name, url);
    assertEquals("The name of the module should be the same as that given to the constructor",
        name, module.getName());
    assertEquals("The URL of the module should be the same as that given to the constructor",
        url, module.getUrl());
  }

  @Test
  public void testCreateModuleFromJsonObject() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\",\"url\":\"https://aalto.fi\"}");
    Module module = Module.fromJsonObject(jsonObject, COURSE_FACTORY);
    assertEquals("The name of the module should be the same as that in the JSON object",
        "Name", module.getName());
    assertEquals("The URL of the module should be the same as that in the JSON object",
        new URL("https://aalto.fi"), module.getUrl());
  }

  @Test(expected = JSONException.class)
  public void testCreateModuleFromJsonObjectMissingName() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"url\":\"https://example.org\"}");
    Module.fromJsonObject(jsonObject, COURSE_FACTORY);
  }

  @Test(expected = JSONException.class)
  public void testCreateModuleFromJsonObjectMissingUrl() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\"}");
    Module.fromJsonObject(jsonObject, COURSE_FACTORY);
  }

  @Test(expected = MalformedURLException.class)
  public void testCreateModuleFromJsonObjectWithMalformedUrl() throws MalformedURLException {
    JSONObject jsonObject = new JSONObject("{\"name\":\"Name\",\"url\":\"\"}");
    Module.fromJsonObject(jsonObject, COURSE_FACTORY);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testStateChanged() throws MalformedURLException {
    URL url = new URL("https://example.com");
    Object listener = new Object();
    Event.Callback<Object> callback = mock(Event.Callback.class);
    Module module = new Module("Changing module", url);
    module.stateChanged.addListener(listener, callback);
    verifyNoInteractions(callback);
    assertEquals(Module.NOT_INSTALLED, module.stateMonitor.get());
    module.stateMonitor.set(Module.FETCHING);
    verify(callback, times(1)).callbackUntyped(listener);
    verifyNoMoreInteractions(callback);
  }
}
