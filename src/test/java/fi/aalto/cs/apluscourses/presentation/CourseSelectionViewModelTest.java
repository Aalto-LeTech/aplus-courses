package fi.aalto.cs.apluscourses.presentation;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class CourseSelectionViewModelTest {

  @Test
  public void testCourseSelectionViewModel() {
    CourseSelectionViewModel viewModel = new CourseSelectionViewModel(Arrays.asList(
        new CourseItemViewModel("A", "A", "http://example.com"),
        new CourseItemViewModel("B", "B", "http://example.com")
    ));

    Assert.assertTrue("The URL field is initially empty",
        viewModel.selectedCourseUrl.get().isEmpty());
    Assert.assertEquals("The view model contains the given courses",
        "A", viewModel.getCourses()[0].getName());
    Assert.assertEquals("The view model contains the given courses",
        "B", viewModel.getCourses()[1].getName());
  }

  @Test
  public void testCourseSelectionValidation() {
    CourseSelectionViewModel viewModel = new CourseSelectionViewModel(Collections.emptyList());

    Assert.assertNotNull("The initially empty URL field is not accepted",
        viewModel.selectedCourseUrl.validate());

    viewModel.selectedCourseUrl.set("abc");
    Assert.assertNotNull("An invalid URL is not accepted",
        viewModel.selectedCourseUrl.validate());

    viewModel.selectedCourseUrl.set("http://localhost:7777");
    Assert.assertNull("A valid URL is accepted", viewModel.selectedCourseUrl.validate());
  }

}
