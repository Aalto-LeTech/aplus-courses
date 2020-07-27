package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.SubmissionResult;
import org.junit.Assert;
import org.junit.Test;

public class SubmissionResultViewModelTest {

  @Test
  public void testSubmissionResultViewModel() {
    SubmissionResult submissionResult = new SubmissionResult(123L, 34);
    SubmissionResultViewModel viewModel = new SubmissionResultViewModel(submissionResult);

    Assert.assertEquals("Submission 34", viewModel.getPresentableName());
    Assert.assertNull(viewModel.getSubtrees());
  }

}
