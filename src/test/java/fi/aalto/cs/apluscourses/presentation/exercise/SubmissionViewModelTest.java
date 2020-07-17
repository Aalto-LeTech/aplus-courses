package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertNotNull;

import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SubmissionViewModelTest {

  @Test
  public void testValidation() {
    String fileName = "some.file";
    SubmittableFile file = new SubmittableFile(fileName);
    SubmittableFile[] files = new SubmittableFile[] { file };

    Exercise exercise = new Exercise(100, "Exercise");
    SubmissionInfo submissionInfo = new SubmissionInfo(4, files);

    SubmissionHistory history = new SubmissionHistory(4);

    List<String> names = new ArrayList<>();
    names.add("Som Eone");

    Group group = new Group(200, names);
    List<Group> groups = new ArrayList<>();
    groups.add(group);

    Path path = Paths.get(fileName);
    Path[] paths = new Path[] { path };

    SubmissionViewModel submissionViewModel =
        new SubmissionViewModel(exercise, submissionInfo, history, groups, paths);

    assertNotNull("The validation should fail when no group is yet selected",
        submissionViewModel.selectedGroup.validate());
    assertNotNull("Submission count should be invalid",
        submissionViewModel.validateSubmissionCount());
  }

}
