package fi.aalto.cs.apluscourses.presentation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

public class CourseSelectionViewModelTest {

  @Test
  public void testCourseSelectionViewModel() {
    CourseSelectionViewModel viewModel = new CourseSelectionViewModel(List.of(
        new CourseItemViewModel("A", "A", "http://example.com"),
        new CourseItemViewModel("B", "B", "http://example.com")
    ));

    Assert.assertTrue("The URL field is initially empty",
        Objects.requireNonNull(viewModel.selectedCourseUrl.get()).isEmpty());
    Assert.assertEquals("The view model contains the given courses",
        "A", Objects.requireNonNull(viewModel.courses.get())[0].getName());
    Assert.assertEquals("The view model contains the given courses",
        "B", Objects.requireNonNull(viewModel.courses.get())[1].getName());
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
