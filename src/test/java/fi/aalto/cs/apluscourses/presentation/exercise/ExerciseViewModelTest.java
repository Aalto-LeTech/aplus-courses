package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseViewModelTest {

  @Test
  public void testGetPresentableName() {
    Exercise exercise1
        = new Exercise(123, "|en:Assignment|fi:Tehtava|", "http://localhost:1000",
        Collections.emptyList(), 0, 0,0);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);

    Exercise exercise2 = new Exercise(234, "Just a name", "http://localhost:2000",
        Collections.emptyList(), 0, 0, 0);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assert.assertEquals("getPresentableName returns the English name of the exercise",
        "Assignment", viewModel1.getPresentableName());
    Assert.assertEquals("getPresentableName returns the name of the exercise",
        "Just a name", viewModel2.getPresentableName());
  }

  @Test
  public void testIsSubmittable() {
    ExerciseViewModel ex1 = new ExerciseViewModel(
        new Exercise(1, "|en:Assignment 13|fi:Tehtava 13|", "http://localhost:3000",
            Collections.emptyList(), 0, 0, 0));
    ExerciseViewModel ex2 = new ExerciseViewModel(
        new Exercise(2, "|en:Assignment 13 (Test)|fi:Tehtava 13 (Test)|", "http://localhost:4000",
            Collections.emptyList(), 0, 0, 0)
    );
    ExerciseViewModel ex3 = new ExerciseViewModel(
        new Exercise(2, "|en:Assignment 1 (Piazza)|fi:Tehtava 1 (Piazza)|", "http://localhost:5000",
            Collections.emptyList(), 0, 0, 0)
    );

    Assert.assertFalse("An assignment without a specific name is not submittable",
        ex1.isSubmittable());
    Assert.assertTrue("An assignment with a specific name is submittable", ex2.isSubmittable());
    Assert.assertFalse("The Piazza assignment is not submittable", ex3.isSubmittable());
  }

  @Test
  public void testGetStatus() {
    String htmlUrl = "http://localhost:6000";
    Exercise noSubmissions = new Exercise(0, "", htmlUrl, Collections.emptyList(), 0, 10, 10);
    Exercise noPoints = new Exercise(0, "", htmlUrl,
        Collections.singletonList(new SubmissionResult(1L, htmlUrl)), 0, 10, 10);
    Exercise partialPoints = new Exercise(0, "", htmlUrl,
        Collections.singletonList(new SubmissionResult(1L, htmlUrl)), 5, 10, 10);
    Exercise fullPoints = new Exercise(0, "", htmlUrl,
        Collections.singletonList(new SubmissionResult(1L, htmlUrl)), 10, 10, 10);

    Assert.assertEquals(ExerciseViewModel.Status.NO_SUBMISSIONS,
        new ExerciseViewModel(noSubmissions).getStatus());
    Assert.assertEquals(ExerciseViewModel.Status.NO_POINTS,
        new ExerciseViewModel(noPoints).getStatus());
    Assert.assertEquals(ExerciseViewModel.Status.PARTIAL_POINTS,
        new ExerciseViewModel(partialPoints).getStatus());
    Assert.assertEquals(ExerciseViewModel.Status.FULL_POINTS,
        new ExerciseViewModel(fullPoints).getStatus());
  }

}
