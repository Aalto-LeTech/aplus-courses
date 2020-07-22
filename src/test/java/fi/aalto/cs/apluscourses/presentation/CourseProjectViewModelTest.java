package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.model.Course;
import java.util.Collections;
import org.junit.Test;

public class CourseProjectViewModelTest {

  private final Course emptyCourse = new Course("123", "NiceCourse", Collections.emptyList(),
      Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(),
      Collections.emptyMap(), Collections.emptyList());


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

    assertTrue("By default the user should want to restart",
        courseProjectViewModel.userWantsRestart());
    assertFalse("By default the user should not want to opt out",
        courseProjectViewModel.userOptsOutOfSettings());

    assertTrue("Restart should be available",
        courseProjectViewModel.isRestartAvailable());
    assertTrue("Settings opt out should be available",
        courseProjectViewModel.canUserOptOutSettings());

    assertTrue("The settings text should mention that IDEA settings will be adjusted",
        courseProjectViewModel.shouldShowSettingsInfo());
  }

  @Test
  public void testIdeSettingsAlreadyImported() {
    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(emptyCourse, "123");

    assertFalse(courseProjectViewModel.userWantsRestart());
    assertTrue(courseProjectViewModel.userOptsOutOfSettings());

    assertFalse("Restart should not be available",
        courseProjectViewModel.isRestartAvailable());
    assertFalse("Settings opt out should not be available",
        courseProjectViewModel.canUserOptOutSettings());

    assertTrue("The settings text should mention that settings are already imported",
        courseProjectViewModel.shouldShowCurrentSettings());
  }

  @Test
  public void testSettingsOptOutMakesRestartUnavailable() {
    CourseProjectViewModel courseProjectViewModel = new CourseProjectViewModel(emptyCourse, "a");

    courseProjectViewModel.settingsOptOutProperty.set(true);
    assertFalse("Setting the settings opt out to true should make the restart option unavailable",
        courseProjectViewModel.isRestartAvailable());
    assertFalse("User should not want a restart after setting the opt out to true",
        courseProjectViewModel.userWantsRestart());

    courseProjectViewModel.settingsOptOutProperty.set(false);
    assertTrue("Setting the settings opt out back to false should make the restart option available"
        + " again", courseProjectViewModel.isRestartAvailable());
    assertFalse("User should still not want a restart",
        courseProjectViewModel.userWantsRestart());
  }
}
