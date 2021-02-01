package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import org.junit.Test;


public class ExercisesTreeViewModelTest {
  @Test
  public void testIsEmptyTextVisible() {
    MainViewModel mainViewModel = new MainViewModel(new Options());

    Course course = new ModelExtensions.TestCourse("TestCourse1");
    mainViewModel.courseViewModel.set(new CourseViewModel(course));
    assertFalse(mainViewModel.exercisesViewModel.get().isEmptyTextVisible());

    mainViewModel.courseViewModel.set(null);
    assertTrue(mainViewModel.exercisesViewModel.get().isEmptyTextVisible());
  }
}
