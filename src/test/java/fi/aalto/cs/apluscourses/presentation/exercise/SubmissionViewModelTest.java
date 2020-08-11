package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertArrayEquals;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class SubmissionViewModelTest {

  @Test
  public void testValidation() {
    List<String> names = new ArrayList<>();
    names.add("Someone");

    Group group = new Group(200, names);
    List<Group> groups = new ArrayList<>();
    groups.add(group);

    String fileName = "some.file";
    Path path = Paths.get(fileName);
    Map<String, Path> fileMap = new HashMap<>();
    fileMap.put("file1", path);

    SubmittableFile file = new SubmittableFile("file1", fileName);
    String language = "en";
    Map<String, List<SubmittableFile>> files
        = Collections.singletonMap(language, Collections.singletonList(file));
    SubmissionInfo submissionInfo = new SubmissionInfo(4, files);

    SubmissionHistory history = new SubmissionHistory(4);

    Exercise exercise = new Exercise(100, "Exercise", "http://localhost:1000",
        Collections.emptyList(), 0, 0, 0);

    SubmissionViewModel submissionViewModel =
        new SubmissionViewModel(exercise, submissionInfo, history, groups, fileMap, language);

    assertNotNull("The validation should fail when no group is yet selected",
        submissionViewModel.selectedGroup.validate());
  }

  @Test
  public void testSubmissionNumbers() {
    Exercise exercise = new Exercise(1, "ex", "http://localhost:2000", Collections.emptyList(),
        0, 0, 0);
    SubmissionInfo info = new SubmissionInfo(5, Collections.emptyMap());

    SubmissionViewModel submissionViewModel1 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(3), Collections.emptyList(), Collections.emptyMap(), "");

    assertEquals(5, submissionViewModel1.getMaxNumberOfSubmissions());
    assertEquals(4, submissionViewModel1.getCurrentSubmissionNumber());
    assertNull(submissionViewModel1.getSubmissionWarning());

    SubmissionViewModel submissionViewModel2 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(4), Collections.emptyList(), Collections.emptyMap(), "");

    assertNotNull(submissionViewModel2.getSubmissionWarning());

    SubmissionViewModel submissionViewModel3 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(5), Collections.emptyList(), Collections.emptyMap(), "");

    assertNotNull(submissionViewModel3.getSubmissionWarning());
  }

  @Test
  public void testGetFiles() {
    Exercise exercise = new Exercise(324, "cool", "http://localhost:1324",
        Collections.emptyList(), 0, 0, 0);

    SubmittableFile englishFile1 = new SubmittableFile("file1", "enFile1");
    SubmittableFile englishFile2 = new SubmittableFile("file2", "enFile2");
    SubmittableFile finnishFile1 = new SubmittableFile("file1", "fiFile1");
    SubmittableFile finnishFile2 = new SubmittableFile("file2", "fiFile2");
    Map<String, List<SubmittableFile>> files = new HashMap<>();
    files.put("en", Arrays.asList(englishFile1, englishFile2));
    files.put("fi", Arrays.asList(finnishFile1, finnishFile2));
    SubmissionInfo info = new SubmissionInfo(10, files);

    SubmissionHistory history = new SubmissionHistory(0);

    SubmissionViewModel submission = new SubmissionViewModel(
        exercise, info, history, Collections.emptyList(), Collections.emptyMap(), "fi"
    );

    assertArrayEquals("getFiles returns the files corresponding to the given language",
        new SubmittableFile[] {finnishFile1, finnishFile2}, submission.getFiles());
  }
}
