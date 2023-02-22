package fi.aalto.cs.apluscourses.presentation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CourseItemViewModelTest {

  @Test
  void testCourseItemViewModel() {
    CourseItemViewModel viewModel = new CourseItemViewModel(
        "Cool Course", "Summer 1980", "http://www.fi"
    );

    Assertions.assertEquals("Cool Course", viewModel.getName(),
        "The name is the same as the one given to the constructor");
    Assertions.assertEquals("Summer 1980", viewModel.getSemester(),
        "The semester is the same as the one given to the constructor");
    Assertions.assertEquals("http://www.fi", viewModel.getUrl(),
        "The URL is the same as the one given to the constructor");
  }

}
