package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import com.intellij.ui.LightColors;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;

public class CourseEndedBannerViewModelTest {
  @Test
  public void testBanner() throws MalformedURLException {
    var course = new ModelExtensions.TestCourse("a");
    var url = new URL("https://example.com");
    var courseProject = new CourseProject(course, url, mock(Project.class));
    var authentication = mock(Authentication.class);
    courseProject.setAuthentication(authentication);

    var banner = new CourseEndedBannerViewModel(courseProject);
    assertEquals(LightColors.RED, banner.color.get());

    assertEquals("", banner.text.get());
    banner.update();
    assertEquals("The course has ended.", banner.text.get());
  }
}
