package fi.aalto.cs.apluscourses.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.Version;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CourseTest {

  private static final ModelFactory MODEL_FACTORY = new ModelExtensions.TestModelFactory() {
  };

  @Test
  public void testCreateCourse() throws MalformedURLException {
    String module1name = "Module1";
    Module module1 = new ModelExtensions.TestModule(module1name);
    Module module2 = new ModelExtensions.TestModule("Module2");
    final List<Module> modules = List.of(module1, module2);
    Map<String, URL> resourceUrls = new HashMap<>();
    resourceUrls.put("key", new URL("http://localhost:8000"));
    resourceUrls.put("ideSettings", new URL("http://localhost:23333"));
    Map<String, String> vmOptions = new HashMap<>();
    vmOptions.put("some-option", "nice-value");
    List<String> autoInstallComponents = List.of(module1name);
    Map<String, String[]> replInitialCommands = new HashMap<>();
    replInitialCommands.put("Module1", new String[] {"import o1._"});
    Course course = new ModelExtensions.TestCourse(
        "13",
        "Tester Course",
        "http://localhost:2466/",
        List.of("se", "en"),
        modules,
        // libraries
        Collections.emptyList(),
        // exerciseModules
        Collections.emptyMap(),
        resourceUrls,
        vmOptions,
        autoInstallComponents,
        replInitialCommands,
        BuildInfo.INSTANCE.courseVersion,
        Collections.emptyMap());
    assertEquals("The ID of the course should be the same as that given to the constructor",
        "13", course.getId());
    assertEquals("The name of the course should be the same as that given to the constructor",
        "Tester Course", course.getName());
    assertEquals("The A+ URL of the course should be the same as that given to the constructor",
        "http://localhost:2466/", course.getHtmlUrl());
    assertEquals("The languages of the course should be the ones given to the constructor",
        "se", course.getLanguages().get(0));
    assertEquals("The languages of the course should be the ones given to the constructor",
        "en", course.getLanguages().get(1));
    assertEquals("http://localhost:2466/api/v2/", course.getApiUrl());
    assertEquals("The modules of the course should be the same as those given to the constructor",
        module1name, course.getModules().get(0).getName());
    assertEquals("The modules of the course should be the same as those given to the constructor",
        "Module2", course.getModules().get(1).getName());
    assertEquals(
        "The resource URLs of the course should the same as those given to the constructor",
        new URL("http://localhost:8000"), course.getResourceUrls().get("key"));
    assertEquals("The IDE settings path should be the same as the one given to the constructor",
        new URL("http://localhost:23333"), course.getAppropriateIdeSettingsUrl());
    assertEquals("The VM options should be the same as those given to the constructor",
        "nice-value", course.getVMOptions().get("some-option"));
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
    Course course = new ModelExtensions.TestCourse(
        // id
        "",
        // name
        "",
        "http://localhost:2736",
        Collections.emptyList(),
        // modules
        List.of(module1, module2),
        // libraries
        Collections.emptyList(),
        // exerciseModules
        Collections.emptyMap(),
        // resourceUrls
        Collections.emptyMap(),
        // vmOptions
        Collections.emptyMap(),
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        // tutorials
        Collections.emptyMap());
    assertSame("Course#getModule should return the correct module",
        module2, course.getComponent("Awesome Module"));
  }

  @Test
  public void testGetAutoInstallComponents() throws MalformedURLException {
    String moduleName = "test-module";
    String libraryName = "test-library";
    Module module = new ModelExtensions.TestModule(
        moduleName, new URL("http://localhost:3000"), new Version(2, 3), null, "changes", null);
    Library library = new ModelExtensions.TestLibrary(libraryName);
    Course course = new ModelExtensions.TestCourse(
        // id
        "",
        // name
        "",
        "http://localhost:5555",
        Collections.emptyList(),
        // modules
        List.of(module),
        // libraries
        List.of(library),
        // exerciseModules
        Collections.emptyMap(),
        // resourceUrls
        Collections.emptyMap(),
        // vmOptions
        Collections.emptyMap(),
        // autoInstallComponentNames
        List.of("test-module", "test-library"),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        // tutorials
        Collections.emptyMap());
    List<Component> autoInstalls = course.getAutoInstallComponents();
    assertEquals("The course has the correct auto-install components", 2, autoInstalls.size());
    assertEquals(moduleName, autoInstalls.get(0).getName());
    assertEquals(libraryName, autoInstalls.get(1).getName());
  }

  @Test(expected = NoSuchComponentException.class)
  public void testGetModuleWithMissingModule() throws NoSuchComponentException {
    Course course = new ModelExtensions.TestCourse(
        "Just some ID",
        "Just some course",
        "http://localhost:1951",
        Collections.emptyList(),
        // modules
        Collections.emptyList(),
        // libraries
        Collections.emptyList(),
        // exerciseModules
        Collections.emptyMap(),
        // resourceUrls
        Collections.emptyMap(),
        // vmOptions
        Collections.emptyMap(),
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        // tutorials
        Collections.emptyMap());
    course.getComponent("Test Module");
  }

  private static String idJson = "\"id\":\"1238\"";
  private static String nameJson = "\"name\":\"Awesome Course\"";
  private static String urlJson = "\"aPlusUrl\":\"https://example.fi\"";
  private static String languagesJson = "\"languages\":[\"fi\",\"en\"]";
  private static String modulesJson = "\"modules\":[{\"name\":\"O1Library\",\"url\":"
      + "\"https://wikipedia.org\"},{\"name\":\"GoodStuff\",\"url\":\"https://example.com\"}]";
  private static String exerciseModulesJson = "\"exerciseModules\":{123:{\"en\":\"en_module\"}}";
  private static String resourcesJson = "\"resources\":{\"abc\":\"http://example.com\","
      + "\"def\":\"http://example.org\"}";
  private static String vmOptionsJson = "\"vmOptions\":{\"a\":\"bcd\"}";
  private static String autoInstallJson = "\"autoInstall\":[\"O1Library\"]";
  private static String replInitialCommands = "\"repl\": {\"initialCommands\": {\"GoodStuff\": ["
      + "\"import o1._\",\"import o1.goodstuff._\"]}}";
  private static String courseVersion = "\"version\": \"5.8\"";

  @Test
  public void testFromConfigurationFile() throws MalformedCourseConfigurationException {
    StringReader stringReader = new StringReader("{" + idJson + "," + nameJson + "," + urlJson
        + "," + languagesJson + "," + modulesJson + "," + exerciseModulesJson + "," + resourcesJson
        + "," + vmOptionsJson + "," + autoInstallJson + "," + replInitialCommands + "," + courseVersion + "}");
    Course course = Course.fromConfigurationData(stringReader, "./path/to/file", MODEL_FACTORY);
    assertEquals("Course should have the same ID as that in the configuration JSON",
        "1238", course.getId());
    assertEquals("Course should have the same name as that in the configuration JSON",
        "Awesome Course", course.getName());
    assertEquals("Course should have the same URL as that in the configuration JSON",
        "https://example.fi", course.getHtmlUrl());
    assertEquals("The course should have the languages of the configuration JSON",
        "fi", course.getLanguages().get(0));
    assertEquals("The course should have the languages of the configuration JSON",
        "en", course.getLanguages().get(1));
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
    assertEquals("The course should have the VM options of the configuration JSON",
        "bcd", course.getVMOptions().get("a"));
    assertEquals("The course should have the auto-install components of the configuration JSON",
        "O1Library", course.getAutoInstallComponents().get(0).getName());
    assertEquals("The course should have the REPL initial commands of the configuration JSON",
        "import o1._", course.getReplInitialCommands().get("GoodStuff")[0]);
    assertEquals("The course should have the REPL initial commands of the configuration JSON",
        "import o1.goodstuff._", course.getReplInitialCommands().get("GoodStuff")[1]);
    assertEquals("Course should have the same version as that in the configuration JSON",
        "5.8", course.getVersion().toString());
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationFileMissingId()
      throws MalformedCourseConfigurationException {
    StringReader stringReader = new StringReader(
        "{" + nameJson + "," + urlJson + "," + languagesJson + "," + modulesJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationFileMissingName()
      throws MalformedCourseConfigurationException {
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + urlJson + "," + languagesJson + "," + modulesJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationFileMissingUrl()
      throws MalformedCourseConfigurationException {
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + nameJson + "," + languagesJson + "," + modulesJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationFileMissingLanguages()
      throws MalformedCourseConfigurationException {
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + nameJson + "," + urlJson + "," + modulesJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationFileMissingModules()
      throws MalformedCourseConfigurationException {
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + nameJson + "," + languagesJson + "," + urlJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationFileWithoutJson()
      throws MalformedCourseConfigurationException {
    StringReader stringReader = new StringReader("random text");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationFileWithInvalidModules()
      throws MalformedCourseConfigurationException {
    String modules = "\"modules\":[1,2,3,4]";
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + nameJson + "," + urlJson + "," + languagesJson + "," + modules + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationFileWithInvalidAutoInstalls()
      throws MalformedCourseConfigurationException {
    String autoInstalls = "\"autoInstall\":[1,2,3,4]";
    StringReader stringReader = new StringReader("{" + idJson + "," + nameJson + "," + urlJson
        + "," + languagesJson + "," + modulesJson + "," + autoInstalls + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

  @Test(expected = MalformedCourseConfigurationException.class)
  public void testFromConfigurationWithMalformedReplInitialCommands()
      throws MalformedCourseConfigurationException {
    String replJson = "\"repl\": {\"initialCommands\": []}";
    StringReader stringReader = new StringReader("{" + idJson + "," + nameJson + "," + urlJson
        + "," + languagesJson + "," + replJson + "}");
    Course.fromConfigurationData(stringReader, MODEL_FACTORY);
  }

}
