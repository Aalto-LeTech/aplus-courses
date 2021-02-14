package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseViewModelTest {

  @Test
  public void testGetPresentableName() {
    Exercise exercise1
        = new Exercise(123, "|en:Assignment|fi:Tehtava|", "http://localhost:1000", 0, 0,0, true,
        null);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);

    Exercise exercise2 = new Exercise(234, "Just a name", "http://localhost:2000", 0, 0, 0, false,
        null);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assert.assertEquals("getPresentableName returns the English name of the exercise",
        "Assignment", viewModel1.getPresentableName());
    Assert.assertEquals("getPresentableName returns the name of the exercise",
        "Just a name", viewModel2.getPresentableName());
  }

  @Test
  public void testIsSubmittable() {
    Exercise submittable = new Exercise(0, "", "http://abc.org", 0, 0, 0, true, null);
    Exercise notSubmittable = new Exercise(0, "", "http://def.org", 0, 0, 0, false, null);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(submittable);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(notSubmittable);

    Assert.assertTrue(viewModel1.isSubmittable());
    Assert.assertFalse(viewModel2.isSubmittable());
  }

  @Test
  public void testGetStatus() {
    String htmlUrl = "http://localhost:6000";
    SubmissionResult.Status resultStatus = SubmissionResult.Status.GRADED;
    Exercise training = new Exercise(0, "", htmlUrl, 0, 0, 0, false, null);
    training.addSubmissionResult(new SubmissionResult(1L, 0, resultStatus, training, 0.0));
    Exercise noPoints = new Exercise(0, "", htmlUrl, 0, 10, 10, true, null);
    noPoints.addSubmissionResult(new SubmissionResult(1L, 0, resultStatus, noPoints, 0.0));
    Exercise partialPoints = new Exercise(0, "", htmlUrl, 5, 10, 10, false, null);
    partialPoints.addSubmissionResult(new SubmissionResult(1L,5,resultStatus,partialPoints,0.0));
    Exercise fullPoints = new Exercise(0, "", htmlUrl, 10, 10, 10, true, null);
    fullPoints.addSubmissionResult(new SubmissionResult(1L, 10, resultStatus, fullPoints, 0.0));
    Exercise noSubmissions = new Exercise(0, "", htmlUrl, 0, 10, 10, false, null);

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
    Exercise exercise1 = new Exercise(0, "", "http://localhost:1212", 3, 49, 12, false, null);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);
    Exercise exercise2 = new Exercise(0, "", "http://localhost:2121", 0, 0, 0, false, null);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);
    Exercise exercise3 = new Exercise(0, "Feedback", "http://localhost:9999", 0, 0, 0, true, null);
    ExerciseViewModel viewModel3 = new ExerciseViewModel(exercise3);

    Assert.assertEquals("The status text is correct", "0 of 12, 3/49", viewModel1.getStatusText());
    Assert.assertEquals("The status text is correct",
        "optional practice", viewModel2.getStatusText());
    Assert.assertTrue("The status text is empty for a feedback assignment",
        viewModel3.getStatusText().isEmpty());
  }

  @Test
  public void testGetSearchableString() {
    String name = "Sample name";

    Exercise exercise1 = new Exercise(0, name, "http://abc.org", 0, 0, 0, true, null);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);
    Exercise exercise2 = new Exercise(0, "", "http://abc2.org", 0, 0, 0, false, null);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assert.assertEquals(name, viewModel1.getSearchableString());
    Assert.assertEquals("", viewModel2.getSearchableString());
  }

}
