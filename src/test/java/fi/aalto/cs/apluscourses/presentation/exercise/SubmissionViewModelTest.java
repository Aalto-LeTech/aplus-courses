package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertNotNull;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class SubmissionViewModelTest {

  @Test
  public void testValidation() {
    List<String> names = new ArrayList<>();
    names.add("Som Eone");

    Group group = new Group(200, names);
    List<Group> groups = new ArrayList<>();
    groups.add(group);

    String fileName = "some.file";
    Path path = Paths.get(fileName);
    Map<String, Path> fileMap = new HashMap<>();
    fileMap.put("file1", path);

    SubmittableFile file = new SubmittableFile("file1", fileName);
    SubmittableFile[] files = new SubmittableFile[] { file };
    SubmissionInfo submissionInfo = new SubmissionInfo(4, files);

    SubmissionHistory history = new SubmissionHistory(4);

    Exercise exercise = new Exercise(100, "Exercise");

    SubmissionViewModel submissionViewModel =
        new SubmissionViewModel(exercise, submissionInfo, history, groups, fileMap);

    assertNotNull("The validation should fail when no group is yet selected",
        submissionViewModel.selectedGroup.validate());
    assertNotNull("Submission count should be invalid",
        submissionViewModel.validateSubmissionCount());
  }

}
