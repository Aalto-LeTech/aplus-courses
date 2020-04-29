package fi.aalto.cs.apluscourses.intellij.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.HeavyPlatformTestCase;
import fi.aalto.cs.apluscourses.TestHelper;
import fi.aalto.cs.apluscourses.model.ComponentLoadException;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class IntelliJModelFactoryTest extends HeavyPlatformTestCase implements TestHelper {

  public static final String GOOD_STUFF = "GoodStuff";
  public static final String GOOD_STUFF_ID = "GoodStuffId";
  public static final String O_1_LIBRARY = "O1Library";
  public static final String O_1_LIBRARY_ID = "O1LibraryId";
  public static final String INDEPENDENT_MODULE = "IndependentModule";
  public static final String INDEPENDENT_MODULE_ID = "IMId";

  @Test
  public void testMarkDependentModulesWithValidInputWorks() throws ComponentLoadException {
    //  given
    //  project & modules
    Project project = getProject();
    createAndAddModule(project, GOOD_STUFF, GOOD_STUFF_ID);
    createAndAddModule(project, O_1_LIBRARY, O_1_LIBRARY_ID);

    //  course & modules
    IntelliJModelFactory factory = new IntelliJModelFactory(getProject());
    Module module1 = new ModelExtensions.TestModule(O_1_LIBRARY);
    Module module2 = new ModelExtensions.TestModule(GOOD_STUFF);
    Module spyModule2 = spy(module2);
    when(spyModule2.getDependencies()).thenReturn(Collections.singletonList(O_1_LIBRARY));
    List<Module> modules = Arrays.asList(module1, spyModule2);
    IntelliJCourse course = (IntelliJCourse) factory.createCourse("Tester Course",
        modules, Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());

    //  when
    factory.markDependentModulesInvalid(course, O_1_LIBRARY);

    //then
    assertEquals("State of the dependant " + GOOD_STUFF + " + is errored (-1).",
        -1, module2.stateMonitor.get());
  }

  @Test
  public void testMarkDependentModulesWithIndependentModuleWorks() throws ComponentLoadException {
    //  given
    //  project & modules
    Project project = getProject();
    createAndAddModule(project, INDEPENDENT_MODULE, INDEPENDENT_MODULE_ID);
    createAndAddModule(project, O_1_LIBRARY, O_1_LIBRARY_ID);

    //  course & modules
    IntelliJModelFactory factory = new IntelliJModelFactory(getProject());
    Module module1 = new ModelExtensions.TestModule(O_1_LIBRARY);
    Module module2 = new ModelExtensions.TestModule(INDEPENDENT_MODULE);
    List<Module> modules = Arrays.asList(module1, module2);
    IntelliJCourse course = (IntelliJCourse) factory.createCourse("Tester Course",
        modules, Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());

    //  when
    factory.markDependentModulesInvalid(course, O_1_LIBRARY);

    //then
    assertEquals("State of the " + INDEPENDENT_MODULE + " Module has not changed (0).",
        0, module2.stateMonitor.get());
  }

  @Test
  public void testMarkDependentModulesWithInputWorks() throws ComponentLoadException {
    //  given
    //  project & modules
    Project project = getProject();
    createAndAddModule(project, GOOD_STUFF, GOOD_STUFF_ID);
    createAndAddModule(project, O_1_LIBRARY, O_1_LIBRARY_ID);
    createAndAddModule(project, INDEPENDENT_MODULE, INDEPENDENT_MODULE_ID);

    //  course & modules
    IntelliJModelFactory factory = new IntelliJModelFactory(getProject());
    Module module1 = new ModelExtensions.TestModule(O_1_LIBRARY);
    Module module2 = new ModelExtensions.TestModule(GOOD_STUFF);
    Module spyModule2 = spy(module2);
    when(spyModule2.getDependencies()).thenReturn(Collections.singletonList(O_1_LIBRARY));
    List<Module> modules = Arrays.asList(module1, spyModule2);
    IntelliJCourse course = (IntelliJCourse) factory.createCourse("Tester Course",
        modules, Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());

    //  when
    factory.markDependentModulesInvalid(course, INDEPENDENT_MODULE);

    //then
    assertEquals("State of the dependant " + GOOD_STUFF + " + has not changed (0).",
        0, module2.stateMonitor.get());
    assertEquals("State of the " + INDEPENDENT_MODULE + " Module has not changed (0).",
        0, module2.stateMonitor.get());
  }

  @Test
  public void testGetCourseModuleDependenciesWithInvalidInputWorks() throws ComponentLoadException {
    //  given
    //  course & modules
    IntelliJModelFactory factory = new IntelliJModelFactory(getProject());
    Module module2 = mock(Module.class);
    when(module2.getDependencies()).thenThrow(new ComponentLoadException("ex", new Throwable()));

    //  when
    List<String> courseModuleDependencies = factory.getCourseModuleDependencies(module2);

    //  then
    assertNull("Returned list is null.", courseModuleDependencies);
  }

  @Test
  public void testGetCourseModuleDependenciesWithValidInputWorks() throws ComponentLoadException {
    //  given
    //  course & modules
    IntelliJModelFactory factory = new IntelliJModelFactory(getProject());
    Module module2 = mock(Module.class);
    when(module2.getDependencies()).thenReturn(Collections.singletonList(O_1_LIBRARY));

    //  when
    List<String> courseModuleDependencies = factory.getCourseModuleDependencies(module2);

    //  then
    assertNotNull("Returned list is not null.", courseModuleDependencies);
    assertTrue("Returns a List containing the correct dependent Module name(s).",
        courseModuleDependencies.contains(O_1_LIBRARY));
  }
}