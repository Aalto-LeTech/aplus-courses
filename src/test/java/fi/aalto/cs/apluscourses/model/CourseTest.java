package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
  public void testCreateCourse() {
    Module module1 = new ModelExtensions.TestModule("Module1");
    Module module2 = new ModelExtensions.TestModule("Module2");
    List<Module> modules = Arrays.asList(module1, module2);
    Map<String, String> requiredPlugins = new HashMap<>();
    requiredPlugins.put("org.intellij.awesome_plugin", "Awesome Plugin");
    Course course = new Course("Tester Course", modules, requiredPlugins);
    assertEquals("The name of the course should be the same as that given to the constructor",
        "Tester Course", course.getName());
    assertEquals("The modules of the course should be the same as those given to the constructor",
        "Module1", course.getModules().get(0).getName());
    assertEquals("The modules of the course should be the same as those given to the constructor",
        "Module2", course.getModules().get(1).getName());
    assertEquals("The required plugins of the course should be the same as those given to the "
            + "constructor", "Awesome Plugin",
        course.getRequiredPlugins().get("org.intellij.awesome_plugin"));
  }

  @Test
  public void testGetModule() throws MalformedURLException, NoSuchModuleException {
    Module module1 = new ModelExtensions.TestModule("Test Module", new URL("https://example.com"));
    Module module2 = new ModelExtensions.TestModule("Awesome Module", new URL("https://slack.com"));
    Course course = new Course("", Arrays.asList(module1, module2), new HashMap<>());
    assertEquals("Course#getModule should return the correct module",
        "Awesome Module", course.getModule("Awesome Module").getName());
    assertEquals("Course#getModule should return the correct module",
        new URL("https://slack.com"), course.getModule("Awesome Module").getUrl());
  }

  @Test(expected = NoSuchModuleException.class)
  public void testGetModuleWithMissingModule() throws NoSuchModuleException {
    Course course = new Course("Just some course", Collections.emptyList(), Collections.emptyMap());
    course.getModule("Test Module");
  }

  @Test
  public void testGetModuleOpt() throws MalformedURLException {
    Module module1 = new ModelExtensions.TestModule("Test test", new URL("https://duckduckgo.com"));
    Module module2 = new ModelExtensions.TestModule("Awesome Module", new URL("https://gmail.com"));
    Course course = new Course("", Arrays.asList(module1, module2), new HashMap<>());
    Module optModule = course.getModuleOpt("Awesome Module");
    assertNotNull("Course#getModuleOpt should not return null for an existing module", optModule);
    assertEquals("Course#getModuleOpt should return the correct module",
        "Awesome Module", optModule.getName());
    assertEquals("Course#getModuleOpt should return the correct module",
        new URL("https://gmail.com"), optModule.getUrl());
  }

  @Test
  public void testGetModuleOptWithMissingModule() {
    Course course = new Course("Meaningless Name", Collections.emptyList(), Collections.emptyMap());
    assertNull("Course#getModuleOpt should return null for a missing module",
        course.getModuleOpt("No module has this name"));
  }

  private static String nameJson = "\"name\":\"Awesome Course\"";
  private static String requiredPluginsJson = "\"requiredPlugins\":{\"org.intellij.scala\":"
      + "\"Scala\",\"org.test.tester\":\"Tester\"}";
  private static String modulesJson = "\"modules\":[{\"name\":\"O1Library\",\"url\":"
      + "\"https://wikipedia.org\"},{\"name\":\"GoodStuff\",\"url\":\"https://example.com\"}]";

  @Test
  public void testFromConfigurationFile() throws MalformedCourseConfigurationFileException {
    StringReader stringReader
        = new StringReader("{" + nameJson + "," + requiredPluginsJson + "," + modulesJson + "}");
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
