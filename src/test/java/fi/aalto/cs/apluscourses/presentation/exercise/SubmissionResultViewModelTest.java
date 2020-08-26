package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class SubmissionResultViewModelTest {

  @Test
  public void testSubmissionResultViewModel() {

    SubmissionResult submissionResult
        = new SubmissionResult(123L, 15,"https://example.com/", SubmissionResult.Status.UNKNOWN);
    ExerciseViewModel exerciseViewModel = new ExerciseViewModel(
        new Exercise(0, "", "", Collections.singletonList(submissionResult),
            15, 25, 10));
    SubmissionResultViewModel viewModel
        = new SubmissionResultViewModel(exerciseViewModel, submissionResult, 34);

    Assert.assertEquals("Submission 34", viewModel.getPresentableName());
    Assert.assertEquals("15/25 points", viewModel.getStatusText());
    Assert.assertNull(viewModel.getSubtrees());
  }

}
