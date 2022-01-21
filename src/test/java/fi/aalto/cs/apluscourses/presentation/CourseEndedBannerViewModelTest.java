package fi.aalto.cs.apluscourses.presentation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import com.intellij.ui.LightColors;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.utils.async.RepeatedTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseEndedBannerViewModelTest {
  @Test
  void testBanner() {
    var course = new ModelExtensions.TestCourse("a");
    var notifier = mock(Notifier.class);
    var courseProject = new CourseProject(course,
        RepeatedTask.create(() -> {
        }),
        RepeatedTask.create(() -> {
        }),
        mock(Project.class), notifier);
    var authentication = mock(Authentication.class);
    courseProject.setAuthentication(authentication);

    var banner = new CourseEndedBannerViewModel(courseProject, notifier);
    Assertions.assertEquals(LightColors.RED, banner.color.get());

    Assertions.assertEquals("The course has ended.", banner.text.get());
  }
}
