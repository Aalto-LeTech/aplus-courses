package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.model.SubmissionResult;
import org.junit.Test;

public class SubmissionResultViewModelTest {

  @Test
  public void testSubmissionResultViewModel() {
    SubmissionResult submissionResult
        = new SubmissionResult(123L, SubmissionResult.Status.UNKNOWN, "https://example.com/");
    SubmissionResultViewModel viewModel = new SubmissionResultViewModel(submissionResult, 34);

    assertEquals("Submission 34", viewModel.getPresentableName());
    assertTrue(viewModel.getChildren().isEmpty());
  }

}
