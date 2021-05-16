package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import org.junit.Test;

public class CourseEndedBannerViewModelTest {
  @Test
  public void testBanner() {
    var courseProject = mock(CourseProject.class);
    var authentication = mock(Authentication.class);
    when(courseProject.getAuthentication()).thenReturn(authentication);
    var course = new ModelExtensions.TestCourse("a");
    when(courseProject.getCourse()).thenReturn(course);

    var banner = new CourseEndedBannerViewModel(courseProject);

    assertEquals("", banner.text.get());
    banner.update();
    assertEquals("The course has ended.", banner.text.get());
  }
}
