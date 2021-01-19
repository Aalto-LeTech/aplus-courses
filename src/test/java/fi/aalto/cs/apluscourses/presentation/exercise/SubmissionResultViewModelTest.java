package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import org.junit.Test;

public class SubmissionResultViewModelTest {

  @Test
  public void testSubmissionResultViewModel() {
    Exercise exercise = new Exercise(0, "", "", 15, 25, 10, true);
    SubmissionResult submissionResult
        = new SubmissionResult(123L, 15, SubmissionResult.Status.UNKNOWN, exercise);
    SubmissionResultViewModel viewModel = new SubmissionResultViewModel(submissionResult, 34);

    assertEquals("Submission 34", viewModel.getPresentableName());
    assertEquals("In grading", viewModel.getStatusText());
    assertTrue(viewModel.getChildren().isEmpty());
  }

}
