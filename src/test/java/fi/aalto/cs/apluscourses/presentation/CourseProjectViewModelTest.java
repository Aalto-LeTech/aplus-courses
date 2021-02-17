package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

public class CourseProjectViewModelTest {

  private final Course emptyCourse = new ModelExtensions.TestCourse(
      "123",
      "NiceCourse",
      "http://localhost:9999",
      Collections.singletonList("de"),
      //  modules
      Collections.emptyList(),
      //  libraries
      Collections.emptyList(),
      //  exerciseModules
      Collections.emptyMap(),
      //  resourceUrls
      Collections.emptyMap(),
      //  autoInstallComponentNames
      Collections.emptyList(),
      //  replInitialCommands
      Collections.emptyMap()
  );

  @Test
  public void testInformationTextIncludesCourseName() {
    CourseProjectViewModel courseProjectViewModel = new CourseProjectViewModel(emptyCourse, "");
    assertEquals("The information text contains the course name",
        "NiceCourse", courseProjectViewModel.getCourseName());
  }

  @Test
  public void testIdeSettingsNotPreviouslyImported() {
    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(emptyCourse, "different");

    assertFalse("By default the user should not want to opt out",
        courseProjectViewModel.userOptsOutOfSettings());

    assertTrue("Settings opt out should be available",
        courseProjectViewModel.canUserOptOutSettings());

    assertTrue("The settings text should mention that IDEA settings will be adjusted",
        courseProjectViewModel.shouldShowSettingsInfo());
  }

  @Test
  public void testIdeSettingsAlreadyImported() {
    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(emptyCourse, "123");

    assertTrue(courseProjectViewModel.userOptsOutOfSettings());

    assertFalse("Settings opt out should not be available",
        courseProjectViewModel.canUserOptOutSettings());

    assertTrue("The settings text should mention that settings are already imported",
        courseProjectViewModel.shouldShowCurrentSettings());
  }

  @Test
  public void testGetLanguages() {
    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(emptyCourse, "987");
    assertArrayEquals(new String[]{"de"}, courseProjectViewModel.getLanguages());
  }

  @Test
  public void testLanguageSelectionValidation() {
    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(emptyCourse, "111");
    ValidationError error = courseProjectViewModel.languageProperty.validate();
    assertThat(error.getDescription(), containsString("Select a language"));
    courseProjectViewModel.languageProperty.set("fi");
    assertNull(courseProjectViewModel.languageProperty.validate());
  }
}
