package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
  }

  @Test
  public void testSubmissionNumbers() {
    Exercise exercise = new Exercise(1, "ex");
    SubmissionInfo info = new SubmissionInfo(5, new SubmittableFile[0]);

    SubmissionViewModel submissionViewModel1 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(3), Collections.emptyList(), Collections.emptyMap());

    assertEquals(5, submissionViewModel1.getMaxNumberOfSubmissions());
    assertEquals(4, submissionViewModel1.getCurrentSubmissionNumber());
    assertNull(submissionViewModel1.getSubmissionWarning());

    SubmissionViewModel submissionViewModel2 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(4), Collections.emptyList(), Collections.emptyMap());

    assertNotNull(submissionViewModel2.getSubmissionWarning());

    SubmissionViewModel submissionViewModel3 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(5), Collections.emptyList(), Collections.emptyMap());

    assertNotNull(submissionViewModel3.getSubmissionWarning());
  }
}
