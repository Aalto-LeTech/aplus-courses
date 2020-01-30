package fi.aalto.cs.intellij.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CourseTest {

  @Test
  public void testCreateCourse() throws MalformedURLException {
    Module module1 = new Module("Module1", new URL("https://example.com"));
    Module module2 = new Module("Module2", new URL("https://example.org"));
    List<Module> modules = Arrays.asList(module1, module2);
    Map<String, String> requiredPlugins = new HashMap<>();
    requiredPlugins.put("org.intellij.awesome_plugin", "Awesome Plugin");
    Course course = new Course("Awesome Course", modules, requiredPlugins);
    assertEquals("The name of the course should be the same as that given to the constructor",
        course.getName(), "Awesome Course");
    assertEquals("The modules of the course should be the same as those given to the constructor",
        course.getModules().get(0).getName(), "Module1");
    assertEquals("The modules of the course should be the same as those given to the constructor",
        course.getModules().get(1).getName(), "Module2");
    assertEquals("The required plugins of the course should be the same as those given to the "
        + "constructor", course.getRequiredPlugins().get("org.intellij.awesome_plugin"),
        "Awesome Plugin");
  }

  @Test
  public void testCreateEmptyCourse() {
    Course course = Course.createEmptyCourse();
    assertEquals("The name of an empty course should be empty", course.getName(), "");
    assertTrue("An empty course should have no modules", course.getModules().isEmpty());
    assertTrue("An empty course should have no required plugins",
        course.getRequiredPlugins().isEmpty());
  }

  @Test
  public void testGetModuleUrl() throws MalformedURLException, NoSuchModuleException {
    Module module1 = new Module("Test Module", new URL("https://wikipedia.org"));
    Module module2 = new Module("Awesome Module", new URL("https://example.com"));
    Course course = new Course("", Arrays.asList(module1, module2), new HashMap<>());
    assertEquals("Course#getModuleUrl should return the correct URL",
        course.getModuleUrl("Awesome Module"), new URL("https://example.com"));
  }

  @Test(expected = NoSuchModuleException.class)
  public void testGetModuleUrlWithMissingModule() throws NoSuchModuleException {
    Course course = Course.createEmptyCourse();
    course.getModuleUrl("Test Module");
  }

}
