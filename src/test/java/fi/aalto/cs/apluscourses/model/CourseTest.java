package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class CourseTest {

  private static final ModelFactory MODEL_FACTORY = new ModelExtensions.TestModelFactory() {};

  @Test
  public void testCreateCourse() throws MalformedURLException {
    Module module1 = new ModelExtensions.TestModule("Module1");
    Module module2 = new ModelExtensions.TestModule("Module2");
    List<Module> modules = Arrays.asList(module1, module2);
    Map<String, String> requiredPlugins = new HashMap<>();
    requiredPlugins.put("org.intellij.awesome_plugin", "Awesome Plugin");
    Map<String, URL> resourceUrls = new HashMap<>();
    resourceUrls.put("key", new URL("http://localhost:8000"));
    Course course = new Course("Tester Course", modules, Collections.emptyList(),
        requiredPlugins, resourceUrls);
    assertEquals("The name of the course should be the same as that given to the constructor",
        "Tester Course", course.getName());
    assertEquals("The modules of the course should be the same as those given to the constructor",
        "Module1", course.getModules().get(0).getName());
    assertEquals("The modules of the course should be the same as those given to the constructor",
        "Module2", course.getModules().get(1).getName());
    assertEquals("The required plugins of the course should be the same as those given to the "
            + "constructor", "Awesome Plugin",
        course.getRequiredPlugins().get("org.intellij.awesome_plugin"));
    assertEquals(
        "The resource URLs of the course should the same as those given to the constructor",
        new URL("http://localhost:8000"), course.getResourceUrls().get("key"));
  }

  @Test
  public void testGetModule() throws MalformedURLException, NoSuchComponentException {
    Module module1 = new ModelExtensions.TestModule("Test Module", new URL("https://example.com"));
    Module module2 = new ModelExtensions.TestModule("Awesome Module", new URL("https://slack.com"));
    Course course = new Course("", Arrays.asList(module1, module2), Collections.emptyList(),
        Collections.emptyMap(), Collections.emptyMap());
    assertSame("Course#getModule should return the correct module",
        module2, course.getComponent("Awesome Module"));
  }

  @Test(expected = NoSuchComponentException.class)
  public void testGetModuleWithMissingModule() throws NoSuchComponentException {
    Course course = new Course("Just some course", Collections.emptyList(), Collections.emptyList(),
        Collections.emptyMap(), Collections.emptyMap());
    course.getComponent("Test Module");
  }

  private static String nameJson = "\"name\":\"Awesome Course\"";
  private static String requiredPluginsJson = "\"requiredPlugins\":{\"org.intellij.scala\":"
      + "\"Scala\",\"org.test.tester\":\"Tester\"}";
  private static String modulesJson = "\"modules\":[{\"name\":\"O1Library\",\"url\":"
      + "\"https://wikipedia.org\"},{\"name\":\"GoodStuff\",\"url\":\"https://example.com\"}]";
  private static String resourcesJson = "\"resources\":{\"abc\":\"http://example.com\","
      + "\"def\":\"http://example.org\"}";

  @Test
  public void testFromConfigurationFile() throws MalformedCourseConfigurationFileException {
    StringReader stringReader = new StringReader("{" + nameJson + "," + requiredPluginsJson + ","
        + modulesJson + "," + resourcesJson + "}");
    Course course = Course.fromConfigurationData(stringReader, "./path/to/file", MODEL_FACTORY);
    assertEquals("Course should have the same name as that in the configuration JSON",
        "Awesome Course", course.getName());
    assertEquals("The course should have the required plugins of the configuration JSON",
        "Tester", course.getRequiredPlugins().get("org.test.tester"));
    assertEquals("The course should have the required plugins of the configuration JSON",
        "Scala", course.getRequiredPlugins().get("org.intellij.scala"));
    assertEquals("The course should have the modules of the configuration JSON",
        "O1Library", course.getModules().get(0).getName());
    assertEquals("The course should have the modules of the configuration JSON",
        "GoodStuff", course.getModules().get(1).getName());
    assertEquals("The course should have the resource URLs of the configuration JSON",
        "http://example.com", course.getResourceUrls().get("abc").toString());
    assertEquals("The course should have the resource URLs of the configuration JSON",
        "http://example.org", course.getResourceUrls().get("def").toString());
  }

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testFromConfigurationFileMissingName()
      throws MalformedCourseConfigurationFileException {
    StringReader stringReader =
        new StringReader("{" + requiredPluginsJson + "," + modulesJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testFromConfigurationFileMissingRequiredPlugins()
      throws MalformedCourseConfigurationFileException {
    StringReader stringReader =
        new StringReader("{" + nameJson + "," + modulesJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testFromConfigurationFileMissingModules()
      throws MalformedCourseConfigurationFileException {
    StringReader stringReader =
        new StringReader("{" + nameJson + "," + requiredPluginsJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testFromConfigurationFileWithoutJson()
      throws MalformedCourseConfigurationFileException {
    StringReader stringReader = new StringReader("random text");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testFromConfigurationFileWithInvalidRequiredPlugins()
      throws MalformedCourseConfigurationFileException {
    String requiredPlugins = "\"requiredPlugins\":[1,2,3,4]";
    StringReader stringReader
        = new StringReader("{" + nameJson + "," + requiredPlugins + "," + modulesJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testFromConfigurationFileWithInvalidModules()
      throws MalformedCourseConfigurationFileException {
    String modules = "\"modules\":[1,2,3,4]";
    StringReader stringReader
        = new StringReader("{" + nameJson + "," + requiredPluginsJson + "," + modules + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }
}
