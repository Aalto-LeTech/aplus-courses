package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;
import org.junit.Assert;
import org.junit.Test;

public class ExerciseViewModelTest {

  @Test
  public void testGetPresentableName() {
    var info = new SubmissionInfo(Collections.emptyMap());

    Exercise exercise1
        = new Exercise(123, "|en:Assignment|fi:Tehtava|", "http://localhost:1000", info, 0, 0, 0, OptionalLong.empty());
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);

    Exercise exercise2 = new Exercise(234, "Just a name", "http://localhost:2000", info, 0, 0, 0, OptionalLong.empty());
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assert.assertEquals("getPresentableName returns the English name of the exercise",
        "Assignment", viewModel1.getPresentableName());
    Assert.assertEquals("getPresentableName returns the name of the exercise",
        "Just a name", viewModel2.getPresentableName());
  }

  @Test
  public void testIsSubmittable() {
    var info1 = new SubmissionInfo(
        Collections.singletonMap("fi", List.of(new SubmittableFile("file1", "abc")))
    );
    var info2 = new SubmissionInfo(Collections.emptyMap());
    Exercise submittable = new Exercise(0, "", "http://abc.org", info1, 0, 0, 0, OptionalLong.empty());
    Exercise notSubmittable = new Exercise(0, "", "http://def.org", info2, 0, 0, 0, OptionalLong.empty());
    ExerciseViewModel viewModel1 = new ExerciseViewModel(submittable);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(notSubmittable);

    Assert.assertTrue(viewModel1.isSubmittable());
    Assert.assertFalse(viewModel2.isSubmittable());
  }

  @Test
  public void testGetStatus() {
    var info = new SubmissionInfo(Collections.emptyMap());
    String htmlUrl = "http://localhost:6000";
    SubmissionResult.Status resultStatus = SubmissionResult.Status.GRADED;
    Exercise training = new Exercise(0, "", htmlUrl, info, 0, 0, 0, OptionalLong.empty());
    training.addSubmissionResult(new SubmissionResult(1L, 0, 0.0, resultStatus, training));
    Exercise noPoints = new Exercise(0, "", htmlUrl, info, 0, 10, 10, OptionalLong.empty());
    noPoints.addSubmissionResult(new SubmissionResult(1L, 0, 0.0, resultStatus, noPoints));
    Exercise partialPoints = new Exercise(0, "", htmlUrl, info, 5, 10, 10, OptionalLong.empty());
    partialPoints.addSubmissionResult(new SubmissionResult(1L,5,0.0,resultStatus,partialPoints));
    Exercise fullPoints = new Exercise(0, "", htmlUrl, info, 10, 10, 10, OptionalLong.empty());
    fullPoints.addSubmissionResult(new SubmissionResult(1L, 10, 0.0, resultStatus, fullPoints));
    Exercise noSubmissions = new Exercise(0, "", htmlUrl, info, 0, 10, 10, OptionalLong.empty());

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

    training.addSubmissionResult(
        new SubmissionResult(2L, 0, 0.0, SubmissionResult.Status.WAITING, training));
    Assert.assertEquals(ExerciseViewModel.Status.IN_GRADING,
        new ExerciseViewModel(training).getStatus());
  }

  @Test
  public void testGetStatusText() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise1 = new Exercise(0, "", "http://localhost:1212", info, 3, 49, 12, OptionalLong.empty());
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);
    Exercise exercise2 = new Exercise(0, "", "http://localhost:2121", info, 0, 0, 0, OptionalLong.empty());
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);
    Exercise exercise3 = new Exercise(0, "Feedback", "http://localhost:9999", info, 0, 0, 0, OptionalLong.empty());
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
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise1 = new Exercise(0, name, "http://abc.org", info, 0, 0, 0, OptionalLong.empty());
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);
    Exercise exercise2 = new Exercise(0, "", "http://abc2.org", info, 0, 0, 0, OptionalLong.empty());
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assert.assertEquals(name, viewModel1.getSearchableString());
    Assert.assertEquals("", viewModel2.getSearchableString());
  }

}
