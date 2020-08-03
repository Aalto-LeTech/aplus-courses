package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Test;

public class CourseTest {

  private static final ModelFactory MODEL_FACTORY = new ModelExtensions.TestModelFactory() {
  };

  @Test
  public void testCreateCourse() throws MalformedURLException {
    String module1name = "Module1";
    Module module1 = new ModelExtensions.TestModule(module1name);
    Module module2 = new ModelExtensions.TestModule("Module2");
    final List<Module> modules = Arrays.asList(module1, module2);
    Map<String, String> requiredPlugins = new HashMap<>();
    requiredPlugins.put("org.intellij.awesome_plugin", "Awesome Plugin");
    Map<String, URL> resourceUrls = new HashMap<>();
    resourceUrls.put("key", new URL("http://localhost:8000"));
    List<String> autoInstallComponents = Arrays.asList(module1name);
    Map<String, String[]> replInitialCommands = new HashMap<>();
    replInitialCommands.put("Module1", new String[]{"import o1._"});
    Course course = new ModelExtensions.TestCourse("13", "Tester Course", modules,
        Collections.emptyList(), Collections.emptyMap(), requiredPlugins, resourceUrls,
        autoInstallComponents, replInitialCommands);
    assertEquals("The ID of the course should be the same as that given to the constructor",
        "13", course.getId());
    assertEquals("The name of the course should be the same as that given to the constructor",
        "Tester Course", course.getName());
    assertEquals("The modules of the course should be the same as those given to the constructor",
        module1name, course.getModules().get(0).getName());
    assertEquals("The modules of the course should be the same as those given to the constructor",
        "Module2", course.getModules().get(1).getName());
    assertEquals("The required plugins of the course should be the same as those given to the "
            + "constructor", "Awesome Plugin",
        course.getRequiredPlugins().get("org.intellij.awesome_plugin"));
    assertEquals(
        "The resource URLs of the course should the same as those given to the constructor",
        new URL("http://localhost:8000"), course.getResourceUrls().get("key"));
    assertEquals(
        "The auto-install components should be the same as those given to the constructor",
        module1name, course.getAutoInstallComponents().get(0).getName());
    assertEquals("The REPL initial commands for Module1 are correct.", "import o1._",
        course.getReplInitialCommands().get("Module1")[0]);
  }

  @Test
  public void testGetModule() throws NoSuchComponentException {
    Module module1 = new ModelExtensions.TestModule("Test Module");
    Module module2 = new ModelExtensions.TestModule("Awesome Module");
    Course course = new ModelExtensions.TestCourse("", "", Arrays.asList(module1, module2),
        Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(),
        Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
    assertSame("Course#getModule should return the correct module",
        module2, course.getComponent("Awesome Module"));
  }

  @Test
  public void testGetAutoInstallComponents() throws MalformedURLException {
    String moduleName = "test-module";
    String libraryName = "test-library";
    Module module = new ModelExtensions.TestModule(
        moduleName, new URL("http://localhost:3000"), "random", null, null);
    Library library = new ModelExtensions.TestLibrary(libraryName);
    Course course = new ModelExtensions.TestCourse("", "", Arrays.asList(module),
        Arrays.asList(library), Collections.emptyMap(),Collections.emptyMap(),
        Collections.emptyMap(), Arrays.asList("test-module", "test-library"),
        Collections.emptyMap());
    List<Component> autoInstalls = course.getAutoInstallComponents();
    assertEquals("The course has the correct auto-install components", 2, autoInstalls.size());
    assertEquals(moduleName, autoInstalls.get(0).getName());
    assertEquals(libraryName, autoInstalls.get(1).getName());
  }

  @Test(expected = NoSuchComponentException.class)
  public void testGetModuleWithMissingModule() throws NoSuchComponentException {
    Course course = new ModelExtensions.TestCourse("Just some ID", "Just some course",
        Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(),
        Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(),
        Collections.emptyMap());
    course.getComponent("Test Module");
  }

  private static String idJson = "\"id\":\"1238\"";
  private static String nameJson = "\"name\":\"Awesome Course\"";
  private static String requiredPluginsJson = "\"requiredPlugins\":{\"org.intellij.scala\":"
      + "\"Scala\",\"org.test.tester\":\"Tester\"}";
  private static String modulesJson = "\"modules\":[{\"name\":\"O1Library\",\"url\":"
      + "\"https://wikipedia.org\"},{\"name\":\"GoodStuff\",\"url\":\"https://example.com\"}]";
  private static String exerciseModulesJson = "\"exerciseModules\":{123:{\"en\":\"en_module\"}}";
  private static String resourcesJson = "\"resources\":{\"abc\":\"http://example.com\","
      + "\"def\":\"http://example.org\"}";
  private static String autoInstallJson = "\"autoInstall\":[\"O1Library\"]";
  private static String replInitialCommands = "\"repl\": {\"initialCommands\": {\"GoodStuff\": ["
      + "\"import o1._\",\"import o1.goodstuff._\"]}}";

  @Test
  public void testFromConfigurationFile() throws MalformedCourseConfigurationFileException {
    StringReader stringReader = new StringReader("{" + idJson + "," + nameJson + ","
        + requiredPluginsJson + "," + modulesJson + "," + exerciseModulesJson + ","
        + resourcesJson + "," + autoInstallJson + "," + replInitialCommands + "}");
    Course course = Course.fromConfigurationData(stringReader, "./path/to/file", MODEL_FACTORY);
    assertEquals("Course should have the same ID as that in the configuration JSON",
        "1238", course.getId());
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
    assertEquals("The course should have the exercise modules of the configuration JSON",
        "en_module", course.getExerciseModules().get(123L).get("en"));
    assertEquals("The course should have the resource URLs of the configuration JSON",
        "http://example.com", course.getResourceUrls().get("abc").toString());
    assertEquals("The course should have the resource URLs of the configuration JSON",
        "http://example.org", course.getResourceUrls().get("def").toString());
    assertEquals("The course should have the auto-install components of the configuration JSON",
        "O1Library", course.getAutoInstallComponents().get(0).getName());
    assertEquals("The course should have the REPL initial commands of the configuration JSON",
        "import o1._", course.getReplInitialCommands().get("GoodStuff")[0]);
    assertEquals("The course should have the REPL initial commands of the configuration JSON",
        "import o1.goodstuff._", course.getReplInitialCommands().get("GoodStuff")[1]);
  }

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testFromConfigurationFileMissingId()
      throws MalformedCourseConfigurationFileException {
    StringReader stringReader
        = new StringReader("{" + nameJson + "," + modulesJson + "," + requiredPluginsJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
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

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testFromConfigurationFileWithInvalidAutoInstalls()
      throws MalformedCourseConfigurationFileException {
    String autoInstalls = "\"autoInstall\":[1,2,3,4]";
    StringReader stringReader = new StringReader("{" + nameJson + "," + requiredPluginsJson + ","
        + modulesJson + "," + autoInstalls + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test
  public void testGetReplInitialCommandsWorksWithValidInput()
      throws IOException, MalformedCourseConfigurationFileException {
    //  given
    File testDataJson = new File("src/test/resources/replInitialCommandsTestData.json");
    JSONObject replInitialCommandsJson = getJsonObject(testDataJson);

    //  when
    Map<String, String[]> replInitialCommands = Course
        .getReplInitialCommands(replInitialCommandsJson, "");

    //  then
    String[] trainsCommands = replInitialCommands.get("Train");
    assertEquals("Amount of initial commands for module 'Train' is equal to 2 (two).", 2,
        trainsCommands.length);
    assertEquals("The first 'Train' module's command is correct.", "import o1._",
        trainsCommands[0]);
    assertEquals("The second 'Train' module's command is correct.", "import o1.train._",
        trainsCommands[1]);
    String[] o1sCommands = replInitialCommands.get("O1");
    assertEquals("Amount of initial commands for module 'O1' is equal to 1 (one).", 1,
        o1sCommands.length);
    assertEquals("The 'O1' module's command is correct.", "import o1._", o1sCommands[0]);
  }

  @Test(expected = MalformedCourseConfigurationFileException.class)
  public void testGetReplInitialCommandsThrowsWhenShould()
      throws MalformedCourseConfigurationFileException {
    JSONObject wrongJsonStringJson = new JSONObject("{}");

    Course.getReplInitialCommands(wrongJsonStringJson, "");
  }

  public static JSONObject getJsonObject(File file) throws IOException {
    return new JSONObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
  }
}
