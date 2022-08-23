package fi.aalto.cs.apluscourses.presentation.exercise;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.SubmissionFileInfo;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import java.util.Collections;
import java.util.OptionalLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubmissionResultViewModelTest {

  @Test
  void testSubmissionResultViewModel() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise = new Exercise(0, "", "", info, 25, 10, OptionalLong.empty(), null, false);
    SubmissionResult submissionResult
        = new SubmissionResult(123L, 15, 0.0, SubmissionResult.Status.WAITING, exercise, new SubmissionFileInfo[0], -1,
        -1);
    SubmissionResultViewModel viewModel = new SubmissionResultViewModel(submissionResult, 34, () -> true);

    Assertions.assertEquals("Submission 34 (123)", viewModel.getPresentableName());
    Assertions.assertEquals("In grading", viewModel.getStatusText());
    Assertions.assertTrue(viewModel.getChildren().isEmpty());

    SubmissionResultViewModel viewModelNotAssistant = new SubmissionResultViewModel(submissionResult, 34, () -> false);
    Assertions.assertEquals("Submission 34", viewModelNotAssistant.getPresentableName());
  }

}
