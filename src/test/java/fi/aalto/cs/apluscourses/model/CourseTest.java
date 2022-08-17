package fi.aalto.cs.apluscourses.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.Version;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseTest {

  private static final ModelFactory MODEL_FACTORY = new ModelExtensions.TestModelFactory() {
  };

  @Test
  void testCreateCourse() throws MalformedURLException {
    String module1name = "Module1";
    Module module1 = new ModelExtensions.TestModule(module1name);
    Module module2 = new ModelExtensions.TestModule("Module2");
    final List<Module> modules = List.of(module1, module2);
    Map<String, URL> resourceUrls = new HashMap<>();
    resourceUrls.put("key", new URL("http://localhost:8000"));
    resourceUrls.put("ideSettings", new URL("http://localhost:23333"));
    Map<String, String> vmOptions = new HashMap<>();
    vmOptions.put("some-option", "nice-value");
    Set<String> optionalCategories = Set.of("optional-category");
    List<String> autoInstallComponents = List.of(module1name);
    Map<String, String[]> replInitialCommands = new HashMap<>();
    String replAdditionalArguments = "-test";
    replInitialCommands.put("Module1", new String[] {"import o1.*"});
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
        optionalCategories,
        autoInstallComponents,
        replInitialCommands,
        replAdditionalArguments,
        BuildInfo.INSTANCE.courseVersion,
        Collections.emptyMap());
    Assertions.assertEquals("13", course.getId(),
        "The ID of the course should be the same as that given to the constructor");
    Assertions.assertEquals("Tester Course", course.getName(),
        "The name of the course should be the same as that given to the constructor");
    Assertions.assertEquals("http://localhost:2466/", course.getHtmlUrl(),
        "The A+ URL of the course should be the same as that given to the constructor");
    Assertions.assertEquals("se", course.getLanguages().get(0),
        "The languages of the course should be the ones given to the constructor");
    Assertions.assertEquals("en", course.getLanguages().get(1),
        "The languages of the course should be the ones given to the constructor");
    Assertions.assertEquals("http://localhost:2466/api/v2/", course.getApiUrl());
    Assertions.assertEquals(module1name, course.getModules().get(0).getName(),
        "The modules of the course should be the same as those given to the constructor");
    Assertions.assertEquals("Module2", course.getModules().get(1).getName(),
        "The modules of the course should be the same as those given to the constructor");
    Assertions.assertEquals(new URL("http://localhost:8000"), course.getResourceUrls().get("key"),
        "The resource URLs of the course should the same as those given to the constructor");
    Assertions.assertEquals(new URL("http://localhost:23333"), course.getAppropriateIdeSettingsUrl(),
        "The IDE settings path should be the same as the one given to the constructor");
    Assertions.assertEquals("nice-value", course.getVMOptions().get("some-option"),
        "The VM options should be the same as those given to the constructor");
    Assertions.assertEquals("optional-category", course.getOptionalCategories().stream().findFirst().get(),
        "The optional categories should be the same as those given to the constructor");
    Assertions.assertEquals(module1name, course.getAutoInstallComponents().get(0).getName(),
        "The auto-install components should be the same as those given to the constructor");
    Assertions.assertEquals("import o1.*", course.getReplInitialCommands().get("Module1")[0],
        "The REPL initial commands for Module1 are correct.");
    Assertions.assertEquals("-test", course.getReplAdditionalArguments(),
            "The REPL arguments should be the same as that given to the constructor");
  }

  @Test
  void testGetModule() throws NoSuchComponentException {
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
        // optionalCategories
        Collections.emptySet(),
        // autoInstallComponentNames
        Collections.emptyList(),
        // replInitialCommands
        Collections.emptyMap(),
        // replAdditionalArguments
        "",
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        // tutorials
        Collections.emptyMap());
    Assertions.assertSame(module2, course.getComponent("Awesome Module"),
        "Course#getModule should return the correct module");
  }

  @Test
  void testGetAutoInstallComponents() throws MalformedURLException {
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
        // optionalCategories
        Collections.emptySet(),
        // autoInstallComponentNames
        List.of("test-module", "test-library"),
        // replInitialCommands
        Collections.emptyMap(),
        // replAdditionalArguments
        "",
        // courseVersion
        BuildInfo.INSTANCE.courseVersion,
        // tutorials
        Collections.emptyMap());
    List<Component> autoInstalls = course.getAutoInstallComponents();
    Assertions.assertEquals(2, autoInstalls.size(), "The course has the correct auto-install components");
    Assertions.assertEquals(moduleName, autoInstalls.get(0).getName());
    Assertions.assertEquals(libraryName, autoInstalls.get(1).getName());
  }

  @Test
  void testGetModuleWithMissingModule() {
    Course course = new ModelExtensions.TestCourse("Some ID");
    assertThrows(NoSuchComponentException.class, () ->
        course.getComponent("Test Module"));
  }

  private static final String idJson = "\"id\":\"1238\"";
  private static final String nameJson = "\"name\":\"Awesome Course\"";
  private static final String urlJson = "\"aPlusUrl\":\"https://example.fi\"";
  private static final String languagesJson = "\"languages\":[\"fi\",\"en\"]";
  private static final String modulesJson = "\"modules\":[{\"name\":\"O1Library\",\"url\":"
      + "\"https://wikipedia.org\"},{\"name\":\"GoodStuff\",\"url\":\"https://example.com\"}]";
  private static final String exerciseModulesJson = "\"exerciseModules\":{123:{\"en\":\"en_module\"}}";
  private static final String resourcesJson = "\"resources\":{\"abc\":\"http://example.com\","
      + "\"def\":\"http://example.org\"}";
  private static final String vmOptionsJson = "\"vmOptions\":{\"a\":\"bcd\"}";
  private static final String autoInstallJson = "\"autoInstall\":[\"O1Library\"]";
  private static final String replInitialCommands = "\"repl\": {\"initialCommands\": {\"GoodStuff\": ["
      + "\"import o1.*\",\"import o1.goodstuff.*\"]}}";
  private static final String replAdditionalArgumentsJson = "\"replArguments\": \"-test\"";
  private static final String courseVersion = "\"version\": \"5.8\"";

  @Test
  void testFromConfigurationFile() throws MalformedCourseConfigurationException {
    StringReader stringReader = new StringReader("{" + idJson + "," + nameJson + "," + urlJson
        + "," + languagesJson + "," + modulesJson + "," + exerciseModulesJson + "," + resourcesJson
        + "," + vmOptionsJson + "," + autoInstallJson + "," + replInitialCommands
        + "," + replAdditionalArgumentsJson + "," + courseVersion + "}");
    Course course = Course.fromConfigurationData(stringReader, "./path/to/file", MODEL_FACTORY);
    Assertions.assertEquals("1238", course.getId(), "Course should have the same ID as that in the configuration JSON");
    Assertions.assertEquals("Awesome Course", course.getName(),
        "Course should have the same name as that in the configuration JSON");
    Assertions.assertEquals("https://example.fi", course.getHtmlUrl(),
        "Course should have the same URL as that in the configuration JSON");
    Assertions.assertEquals("fi", course.getLanguages().get(0),
        "The course should have the languages of the configuration JSON");
    Assertions.assertEquals("en", course.getLanguages().get(1),
        "The course should have the languages of the configuration JSON");
    Assertions.assertEquals("O1Library", course.getModules().get(0).getName(),
        "The course should have the modules of the configuration JSON");
    Assertions.assertEquals("GoodStuff", course.getModules().get(1).getName(),
        "The course should have the modules of the configuration JSON");
    Assertions.assertEquals("en_module", course.getExerciseModules().get(123L).get("en"),
        "The course should have the exercise modules of the configuration JSON");
    Assertions.assertEquals("http://example.com", course.getResourceUrls().get("abc").toString(),
        "The course should have the resource URLs of the configuration JSON");
    Assertions.assertEquals("http://example.org", course.getResourceUrls().get("def").toString(),
        "The course should have the resource URLs of the configuration JSON");
    Assertions.assertEquals("bcd", course.getVMOptions().get("a"),
        "The course should have the VM options of the configuration JSON");
    Assertions.assertEquals("O1Library", course.getAutoInstallComponents().get(0).getName(),
        "The course should have the auto-install components of the configuration JSON");
    Assertions.assertEquals("import o1.*", course.getReplInitialCommands().get("GoodStuff")[0],
        "The course should have the REPL initial commands of the configuration JSON");
    Assertions.assertEquals("import o1.goodstuff.*", course.getReplInitialCommands().get("GoodStuff")[1],
        "The course should have the REPL initial commands of the configuration JSON");
    Assertions.assertEquals("-test", course.getReplAdditionalArguments(),
        "The course should have the REPL arguments of the configuration JSON");
    Assertions.assertEquals("5.8", course.getVersion().toString(),
        "The course should have the same version as that in the configuration JSON");
  }

  @Test
  void testFromConfigurationFileMissingId() {
    StringReader stringReader = new StringReader(
        "{" + nameJson + "," + urlJson + "," + languagesJson + "," + modulesJson + "}");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

  @Test
  void testFromConfigurationFileMissingName() {
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + urlJson + "," + languagesJson + "," + modulesJson + "}");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

  @Test
  void testFromConfigurationFileMissingUrl() {
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + nameJson + "," + languagesJson + "," + modulesJson + "}");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

  @Test
  void testFromConfigurationFileMissingLanguages() {
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + nameJson + "," + urlJson + "," + modulesJson + "}");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

  @Test
  void testFromConfigurationFileMissingModules() {
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + nameJson + "," + languagesJson + "," + urlJson + "}");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

  @Test
  void testFromConfigurationFileWithoutJson() {
    StringReader stringReader = new StringReader("random text");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

  @Test
  void testFromConfigurationFileWithInvalidModules() {
    String modules = "\"modules\":[1,2,3,4]";
    StringReader stringReader = new StringReader(
        "{" + idJson + "," + nameJson + "," + urlJson + "," + languagesJson + "," + modules + "}");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

  @Test
  void testFromConfigurationFileWithInvalidAutoInstalls() {
    String autoInstalls = "\"autoInstall\":[1,2,3,4]";
    StringReader stringReader = new StringReader("{" + idJson + "," + nameJson + "," + urlJson
        + "," + languagesJson + "," + modulesJson + "," + autoInstalls + "}");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

  @Test
  void testFromConfigurationWithMalformedReplInitialCommands() {
    String replJson = "\"repl\": {\"initialCommands\": []}";
    StringReader stringReader = new StringReader("{" + idJson + "," + nameJson + "," + urlJson
        + "," + languagesJson + "," + replJson + "}");
    assertThrows(MalformedCourseConfigurationException.class, () ->
        Course.fromConfigurationData(stringReader, MODEL_FACTORY));
  }

}
