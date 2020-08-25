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
        new Exercise(1, "|en:Assignment 12|fi:Tehtava 12|", "http://localhost:3000",
            Collections.emptyList(), 0, 10, 5)
    );
    ExerciseViewModel ex2 = new ExerciseViewModel(
        new Exercise(2, "|en:Assignment 13 (Test)|fi:Tehtava 13 (Test)|", "http://localhost:4000",
            Collections.emptyList(), 0, 20, 10)
    );
    ExerciseViewModel ex3 = new ExerciseViewModel(
        new Exercise(4, "|en:Assignment 14 (Practice)|fi:Tehtava 14 (Harjoitus)",
            "http://localhost:5000", Collections.emptyList(), 0, 0, 0)
    );
    ExerciseViewModel ex4 = new ExerciseViewModel(
        new Exercise(4, "|en:Assignment 1 (Piazza)|fi:Tehtava 1 (Piazza)|", "http://localhost:6000",
            Collections.emptyList(), 0, 5, 10)
    );
    ExerciseViewModel ex5 = new ExerciseViewModel(
        new Exercise(4, "|en:Assignment  debugger|fi:Tehtava  debugger|", "http://localhost:6000",
            Collections.emptyList(), 0, 0, 0)
    );

    Assert.assertFalse("An assignment that doesn't have 10 or 0 submissions isn't submittable",
        ex1.isSubmittable());
    Assert.assertTrue("An assignment with 10 submissions is submittable", ex2.isSubmittable());
    Assert.assertTrue("A practice assignment is submittable", ex3.isSubmittable());
    Assert.assertFalse("The Piazza assignment is not submittable", ex4.isSubmittable());
    Assert.assertFalse("The debugger assignment is not submittable", ex5.isSubmittable());
  }

  @Test
  public void testGetStatus() {
    String htmlUrl = "http://localhost:6000";
    SubmissionResult.Status resultStatus = SubmissionResult.Status.GRADED;
    Exercise training = new Exercise(0, "", htmlUrl,
        Collections.singletonList(new SubmissionResult(1L, resultStatus, htmlUrl)), 0, 0, 0);
    Exercise noSubmissions = new Exercise(0, "", htmlUrl, Collections.emptyList(), 0, 10, 10);
    Exercise noPoints = new Exercise(0, "", htmlUrl,
        Collections.singletonList(new SubmissionResult(1L, resultStatus, htmlUrl)), 0, 10, 10);
    Exercise partialPoints = new Exercise(0, "", htmlUrl,
        Collections.singletonList(new SubmissionResult(1L, resultStatus, htmlUrl)), 5, 10, 10);
    Exercise fullPoints = new Exercise(0, "", htmlUrl,
        Collections.singletonList(new SubmissionResult(1L, resultStatus, htmlUrl)), 10, 10, 10);

    Assert.assertEquals(ExerciseViewModel.Status.OPTIONAL_PRACTICE,
        new ExerciseViewModel(training).getStatus());
    Assert.assertEquals(ExerciseViewModel.Status.NO_SUBMISSIONS,
        new ExerciseViewModel(noSubmissions).getStatus());
    Assert.assertEquals(ExerciseViewModel.Status.NO_POINTS,
        new ExerciseViewModel(noPoints).getStatus());
    Assert.assertEquals(ExerciseViewModel.Status.PARTIAL_POINTS,
        new ExerciseViewModel(partialPoints).getStatus());
    Assert.assertEquals(ExerciseViewModel.Status.FULL_POINTS,
        new ExerciseViewModel(fullPoints).getStatus());
  }

  @Test
  public void testGetStatusText() {
    Exercise exercise1 = new Exercise(0, "", "http://localhost:1212", Collections.emptyList(),
        3, 49, 12);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);
    Exercise exercise2 = new Exercise(0, "", "http://localhost:2121", Collections.emptyList(),
        0, 0, 0);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);
    Exercise exercise3 = new Exercise(0, "Feedback", "http://localhost:9999",
        Collections.emptyList(), 0, 0, 0);
    ExerciseViewModel viewModel3 = new ExerciseViewModel(exercise3);

    Assert.assertEquals("The status text is correct", "0 of 12, 3/49", viewModel1.getStatusText());
    Assert.assertEquals("The status text is correct",
        "optional practice", viewModel2.getStatusText());
    Assert.assertTrue("The status text is empty for a feedback assignment",
        viewModel3.getStatusText().isEmpty());
  }

}
