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
  public void testIsSubmittable() {
    ExerciseViewModel ex1 = new ExerciseViewModel(
        new Exercise(1, "|en:Assignment 13|fi:Tehtava 13|"));
    ExerciseViewModel ex2 = new ExerciseViewModel(
        new Exercise(2, "|en:Assignment 13 (Test)|fi:Tehtava 13 (Test)|"));
    ExerciseViewModel ex3 = new ExerciseViewModel(
        new Exercise(2, "|en:Assignment 1 (Piazza)|fi:Tehtava 1 (Piazzza)|"));

    Assert.assertFalse("An assignment without a specific name is not submittable",
        ex1.isSubmittable());
    Assert.assertTrue("An assignment with a specific name is submittable", ex2.isSubmittable());
    Assert.assertFalse("The Piazza assignment is not submittable", ex3.isSubmittable());
  }

}
