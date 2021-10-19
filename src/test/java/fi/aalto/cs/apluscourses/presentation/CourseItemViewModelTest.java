package fi.aalto.cs.apluscourses.presentation;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class CourseItemViewModelTest {

  @Test
  public void testCourseItemViewModel() {
    CourseItemViewModel viewModel = new CourseItemViewModel(
        "Cool Course", "Summer 1980", "http://www.fi"
    );

    Assert.assertEquals("The name is the same as the one given to the constructor",
        "Cool Course", viewModel.getName());
    Assert.assertEquals("The semester is the same as the one given to the constructor",
        "Summer 1980", viewModel.getSemester());
    Assert.assertEquals("The URL is the same as the one given to the constructor",
        "http://www.fi", viewModel.getUrl());
  }

}
