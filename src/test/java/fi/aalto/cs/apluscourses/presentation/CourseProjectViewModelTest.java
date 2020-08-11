package fi.aalto.cs.apluscourses.presentation;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.utils.observable.ValidationError;
import java.util.Collections;
import org.junit.Test;

public class CourseProjectViewModelTest {

  private final Course emptyCourse = new ModelExtensions.TestCourse("123", "NiceCourse",
      Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(),
      Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(),
      Collections.emptyMap());


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
  public void testLanguageSelectionValidation() {
    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(emptyCourse, "111");
    ValidationError error = courseProjectViewModel.languageProperty.validate();
    assertThat(error.getDescription(), containsString("Select a language"));
    courseProjectViewModel.languageProperty.set("fi");
    assertNull(courseProjectViewModel.languageProperty.validate());
  }
}
