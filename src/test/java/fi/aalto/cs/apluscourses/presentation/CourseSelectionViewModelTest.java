package fi.aalto.cs.apluscourses.presentation;

import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseSelectionViewModelTest {

  @Test
  void testCourseSelectionViewModel() {
    CourseSelectionViewModel viewModel = new CourseSelectionViewModel();
    viewModel.courses.set(new CourseItemViewModel[] {
        new CourseItemViewModel("A", "A", "http://example.com", null),
        new CourseItemViewModel("B", "B", "http://example.com", "Scala")
    });

    Assertions.assertTrue(Objects.requireNonNull(viewModel.selectedCourseUrl.get()).isEmpty(),
        "The URL field is initially empty");
    Assertions.assertEquals("A", Objects.requireNonNull(viewModel.courses.get())[0].getName(),
        "The view model contains the given courses");
    Assertions.assertEquals("B", Objects.requireNonNull(viewModel.courses.get())[1].getName(),
        "The view model contains the given courses");
    Assertions.assertEquals(CourseItemViewModel.ProgrammingLanguage.Scala, Objects.requireNonNull(
        viewModel.courses.get())[1].getLanguage());
  }

  @Test
  void testCourseSelectionValidation() {
    CourseSelectionViewModel viewModel = new CourseSelectionViewModel();

    Assertions.assertNotNull(viewModel.selectedCourseUrl.validate(), "The initially empty URL field is not accepted");

    viewModel.selectedCourseUrl.set("abc");
    Assertions.assertNotNull(viewModel.selectedCourseUrl.validate(), "An invalid URL is not accepted");

    viewModel.selectedCourseUrl.set("http://localhost:7777");
    Assertions.assertNull(viewModel.selectedCourseUrl.validate(), "A valid URL is accepted");
  }

}
