package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

    Exercise exercise = new Exercise(100, "Exercise", "http://localhost:1000", 0, 0, 0, true);

    SubmissionViewModel submissionViewModel =
        new SubmissionViewModel(exercise, submissionInfo, history, groups, null, fileMap, language);

    assertNotNull("The validation should fail when no group is yet selected",
        submissionViewModel.selectedGroup.validate());
  }

  @Test
  public void testSubmissionNumbers() {
    Exercise exercise = new Exercise(1, "ex", "http://localhost:2000", 0, 0, 5, true);
    SubmissionInfo info = new SubmissionInfo(5, Collections.emptyMap());

    SubmissionViewModel submissionViewModel1 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(3), Collections.emptyList(), null, Collections.emptyMap(), "");

    assertEquals(4, submissionViewModel1.getCurrentSubmissionNumber());
    assertEquals("You are about to make submission 4 out of 5.",
        submissionViewModel1.getSubmissionCountText());
    assertNull(submissionViewModel1.getSubmissionWarning());

    SubmissionViewModel submissionViewModel2 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(4), Collections.emptyList(), null, Collections.emptyMap(), "");

    assertEquals("You are about to make submission 5 out of 5.",
        submissionViewModel2.getSubmissionCountText());
    assertNotNull(submissionViewModel2.getSubmissionWarning());

    SubmissionViewModel submissionViewModel3 = new SubmissionViewModel(exercise, info,
        new SubmissionHistory(5), Collections.emptyList(), null, Collections.emptyMap(), "");

    assertEquals("You are about to make submission 6 out of 5.",
        submissionViewModel3.getSubmissionCountText());
    assertNotNull(submissionViewModel3.getSubmissionWarning());

    // Max submissions 0
    SubmissionInfo practiceAssignment = new SubmissionInfo(0, Collections.emptyMap());
    SubmissionViewModel submissionViewModel4 = new SubmissionViewModel(exercise, practiceAssignment,
        new SubmissionHistory(2), Collections.emptyList(), null, Collections.emptyMap(), "");

    assertEquals("You are about to make submission 3.",
        submissionViewModel4.getSubmissionCountText());
    assertNull(submissionViewModel4.getSubmissionWarning());
  }

  @Test
  public void testGetFiles() {
    Exercise exercise = new Exercise(324, "cool", "http://localhost:1324", 0, 0, 0, true);

    SubmittableFile englishFile1 = new SubmittableFile("file1", "enFile1");
    SubmittableFile englishFile2 = new SubmittableFile("file2", "enFile2");
    SubmittableFile finnishFile1 = new SubmittableFile("file1", "fiFile1");
    SubmittableFile finnishFile2 = new SubmittableFile("file2", "fiFile2");
    Map<String, List<SubmittableFile>> files = new HashMap<>();
    files.put("en", List.of(englishFile1, englishFile2));
    files.put("fi", List.of(finnishFile1, finnishFile2));
    SubmissionInfo info = new SubmissionInfo(10, files);

    SubmissionHistory history = new SubmissionHistory(0);

    SubmissionViewModel submission = new SubmissionViewModel(
        exercise, info, history, Collections.emptyList(), null, Collections.emptyMap(), "fi"
    );

    assertArrayEquals("getFiles returns the files corresponding to the given language",
        new SubmittableFile[] {finnishFile1, finnishFile2}, submission.getFiles());
  }

  @Test
  public void testDefaultGroup() {
    Exercise exercise = new Exercise(1000, "wow", "http://www.fi", 0, 0, 0, true);
    SubmissionInfo info = new SubmissionInfo(0, Collections.emptyMap());
    SubmissionHistory history = new SubmissionHistory(0);
    Group group = new Group(1, List.of("Jyrki", "Jorma"));
    List<Group> availableGroups = Collections.singletonList(group);

    SubmissionViewModel viewModel1 = new SubmissionViewModel(
        exercise, info, history, availableGroups, null, Collections.emptyMap(), "fi"
    );
    SubmissionViewModel viewModel2 = new SubmissionViewModel(
        exercise, info, history, availableGroups, group, Collections.emptyMap(), "fi"
    );

    assertNull(viewModel1.selectedGroup.get());
    assertNotNull(viewModel2.selectedGroup.get());

    assertFalse("The default group check box is unchecked when no default group exists",
        viewModel1.makeDefaultGroup.get());
    assertTrue("The default group check box is automatically checked if a default group exists",
        viewModel2.makeDefaultGroup.get());
  }
}
