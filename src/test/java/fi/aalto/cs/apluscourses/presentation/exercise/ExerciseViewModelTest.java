package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExerciseViewModelTest {

  @Test
  void testGetPresentableName() {
    var info = new SubmissionInfo(Collections.emptyMap());

    Exercise exercise1 = new Exercise(
        123, "|en:Assignment|fi:Tehtava|", "http://localhost:1000", info, 0, 0,
        OptionalLong.empty(), null, false);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);

    Exercise exercise2 = new Exercise(
        234, "Just a name", "http://localhost:2000", info, 0, 0, OptionalLong.empty(), null, false);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assertions.assertEquals("Assignment", viewModel1.getPresentableName(),
        "getPresentableName returns the English name of the exercise");
    Assertions.assertEquals("Just a name", viewModel2.getPresentableName(),
        "getPresentableName returns the name of the exercise");
  }

  @Test
  void testIsSubmittable() {
    var info1 = new SubmissionInfo(
        Collections.singletonMap("fi", List.of(new SubmittableFile("file1", "abc")))
    );
    var info2 = new SubmissionInfo(Collections.emptyMap());
    Exercise submittable = new Exercise(0, "", "http://abc.org", info1, 0, 0, OptionalLong.empty(), null, false);
    Exercise notSubmittable = new Exercise(0, "", "http://def.org", info2, 0, 0, OptionalLong.empty(), null, false);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(submittable);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(notSubmittable);

    Assertions.assertTrue(viewModel1.isSubmittable());
    Assertions.assertFalse(viewModel2.isSubmittable());
  }

  @Test
  void testGetStatus() {
    var info = new SubmissionInfo(Collections.emptyMap());
    String htmlUrl = "http://localhost:6000";
    SubmissionResult.Status resultStatus = SubmissionResult.Status.GRADED;
    Exercise training = new Exercise(0, "", htmlUrl, info, 1, 10, OptionalLong.empty(), "training", true);
    training.addSubmissionResult(new SubmissionResult(1L, 0, 0.0, resultStatus, training));
    Exercise noPoints = new Exercise(0, "", htmlUrl, info, 10, 10, OptionalLong.empty(), null, false);
    noPoints.addSubmissionResult(new SubmissionResult(1L, 0, 0.0, resultStatus, noPoints));
    Exercise partialPoints = new Exercise(0, "", htmlUrl, info, 10, 10, OptionalLong.of(1L), null, false);
    var partialPointsSubmissionRes = new SubmissionResult(1L, 5, 0.0, SubmissionResult.Status.GRADED, partialPoints);
    partialPoints.addSubmissionResult(partialPointsSubmissionRes);
    partialPoints.addSubmissionResult(new SubmissionResult(1L, 5, 0.0, resultStatus, partialPoints));
    Exercise fullPoints = new Exercise(0, "", htmlUrl, info, 10, 10, OptionalLong.of(2L), null, false);
    var fullPointsSubmissionRes = new SubmissionResult(2L, 10, 0.0, SubmissionResult.Status.GRADED, fullPoints);
    fullPoints.addSubmissionResult(fullPointsSubmissionRes);
    fullPoints.addSubmissionResult(new SubmissionResult(1L, 10, 0.0, resultStatus, fullPoints));
    Exercise noSubmissions = new Exercise(0, "", htmlUrl, info, 10, 10, OptionalLong.empty(), null, false);

    Assertions.assertEquals(ExerciseViewModel.Status.OPTIONAL_PRACTICE, new ExerciseViewModel(training).getStatus());
    Assertions.assertEquals(ExerciseViewModel.Status.NO_SUBMISSIONS, new ExerciseViewModel(noSubmissions).getStatus());
    Assertions.assertEquals(ExerciseViewModel.Status.NO_POINTS, new ExerciseViewModel(noPoints).getStatus());
    Assertions.assertEquals(ExerciseViewModel.Status.PARTIAL_POINTS, new ExerciseViewModel(partialPoints).getStatus());
    Assertions.assertEquals(ExerciseViewModel.Status.FULL_POINTS, new ExerciseViewModel(fullPoints).getStatus());

    training.addSubmissionResult(
        new SubmissionResult(2L, 0, 0.0, SubmissionResult.Status.WAITING, training));
    Assertions.assertEquals(ExerciseViewModel.Status.IN_GRADING, new ExerciseViewModel(training).getStatus());
  }

  @Test
  void testGetStatusText() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise1 = new Exercise(0, "", "http://localhost:1212", info, 49, 12,
        OptionalLong.of(1L), null, false);
    var ex1SubmissionRes = new SubmissionResult(1L, 3, 0.0, SubmissionResult.Status.GRADED, exercise1);
    exercise1.addSubmissionResult(ex1SubmissionRes);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);
    Exercise exercise2 = new Exercise(0, "", "http://localhost:2121", info, 1, 10,
        OptionalLong.empty(), "training", true);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);
    Exercise exercise3 = new Exercise(0, "Feedback", "http://localhost:9999", info, 0, 0,
        OptionalLong.empty(), null, false);
    ExerciseViewModel viewModel3 = new ExerciseViewModel(exercise3);

    Assertions.assertEquals("1 of 12, 3/49", viewModel1.getStatusText(), "The status text is correct");
    Assertions.assertEquals("optional practice", viewModel2.getStatusText(), "The status text is correct");
    Assertions.assertTrue(viewModel3.getStatusText().isEmpty(), "The status text is empty for a feedback assignment");
  }

  @Test
  void testGetSearchableString() {
    String name = "Sample name";
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise1 = new Exercise(0, name, "http://abc.org", info, 0, 0, OptionalLong.empty(), null, false);
    ExerciseViewModel viewModel1 = new ExerciseViewModel(exercise1);
    Exercise exercise2 = new Exercise(0, "", "http://abc2.org", info, 0, 0, OptionalLong.empty(), null, false);
    ExerciseViewModel viewModel2 = new ExerciseViewModel(exercise2);

    Assertions.assertEquals(name, viewModel1.getSearchableString());
    Assertions.assertEquals("", viewModel2.getSearchableString());
  }

}
