package fi.aalto.cs.apluscourses.presentation;

import static org.hamcrest.CoreMatchers.containsString;

import fi.aalto.cs.apluscourses.model.Course;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class CourseProjectViewModelTest {

  private Course emptyCourse = new Course("name", Collections.emptyList(), Collections.emptyList(),
      Collections.emptyMap(), Collections.emptyMap());

  @Test
  public void testInformationTextIncludesCourseName() {
    CourseProjectViewModel courseProjectViewModel = new CourseProjectViewModel(emptyCourse, "");
    Assert.assertThat("The information text contains the course name",
        courseProjectViewModel.getInformationText(), containsString("name"));
  }

  @Test
  public void testIdeSettingsNotPreviouslyImported() {
    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(emptyCourse, "different");

    Assert.assertTrue("By default the user should want to restart",
        courseProjectViewModel.userWantsRestart());
    Assert.assertTrue("By default the user should not want to opt out",
        courseProjectViewModel.userWantsSettings());

    Assert.assertTrue("Restart should be available", courseProjectViewModel.isRestartAvailable());
    Assert.assertTrue("Settings opt out should be available",
        courseProjectViewModel.isOptOutAvailable());

    Assert.assertThat("The settings text should mention that IDEA settings will be adjusted",
        courseProjectViewModel.getSettingsText(), containsString("adjust IntelliJ IDEA settings"));
  }

  @Test
  public void testIdeSettingsAlreadyImported() {
    CourseProjectViewModel courseProjectViewModel
        = new CourseProjectViewModel(emptyCourse, "name");

    Assert.assertFalse(courseProjectViewModel.userWantsRestart());
    Assert.assertFalse(courseProjectViewModel.userWantsSettings());

    Assert.assertFalse("Restart should not be available",
        courseProjectViewModel.isRestartAvailable());
    Assert.assertFalse("Settings opt out should not be available",
        courseProjectViewModel.isOptOutAvailable());

    Assert.assertThat("The settings text should mention that settings are already imported",
        courseProjectViewModel.getSettingsText(),
        containsString("IDEA settings are already imported"));
  }

  @Test
  public void testSettingsOptOutMakesRestartUnavailable() {
    CourseProjectViewModel courseProjectViewModel = new CourseProjectViewModel(emptyCourse, "a");

    courseProjectViewModel.setSettingsOptOut(true);
    Assert.assertFalse("Setting the settings opt out to true should make the restart option "
        + "unavailable", courseProjectViewModel.isRestartAvailable());
    Assert.assertFalse("User should not want a restart after setting the opt out to true",
        courseProjectViewModel.userWantsRestart());

    courseProjectViewModel.setSettingsOptOut(false);
    Assert.assertTrue("Setting the settings opt out back to false should make the restart "
        + "option available again", courseProjectViewModel.isRestartAvailable());
    Assert.assertFalse("User should still not want a restart",
        courseProjectViewModel.userWantsRestart());
  }

  @Test
  public void testCancel() {
    CourseProjectViewModel courseProjectViewModel = new CourseProjectViewModel(emptyCourse, "b");
    courseProjectViewModel.cancel();

    Assert.assertTrue(courseProjectViewModel.userCancels());
    Assert.assertFalse(courseProjectViewModel.userWantsSettings());
    Assert.assertFalse(courseProjectViewModel.userWantsRestart());
  }

}
