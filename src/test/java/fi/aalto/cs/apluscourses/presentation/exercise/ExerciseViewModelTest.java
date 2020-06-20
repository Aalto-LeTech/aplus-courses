package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseViewModelTest {

  @Test
  public void testGetPresentableName() {
    Exercise exercise1 = new Exercise(123, "|en:Assignment|fi:Tehtava|");
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);

    Exercise exercise2 = new Exercise(234, "Just a name");
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assert.assertEquals("getPresentableName returns the English name of the exercise",
        "Assignment", viewModel1.getPresentableName());
    Assert.assertEquals("getPresentableName returns the name of the exercise",
        "Just a name", viewModel2.getPresentableName());
  }

  @Test
  public void testGetAssignmentNumber() {
    Exercise exercise1 = new Exercise(111, "blah blah Assignment 13 blah 7 filler text");
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);

    Exercise exercise2 = new Exercise(222, "No assignment number in this name");
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assert.assertEquals("getAssignmentNumber returns the correct assignment number",
        13, viewModel1.getAssignmentNumber());
    Assert.assertEquals("getAssignmentNumber returns -1 when no assignment number is present",
        -1, viewModel2.getAssignmentNumber());
  }

}
