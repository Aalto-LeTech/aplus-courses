package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.IntStream;
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
    var submissionInfo = new SubmissionInfo(files);
    Exercise exercise = new Exercise(
        100, "Exercise", "http://localhost:1000", submissionInfo, 0, 0, OptionalLong.empty());

    SubmissionViewModel submissionViewModel =
        new SubmissionViewModel(exercise, groups, null, fileMap, language);

    assertNotNull("The validation should fail when no group is yet selected",
        submissionViewModel.selectedGroup.validate());
  }

  @Test
  public void testSubmissionNumbers() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise = new Exercise(1, "ex", "http://localhost:2000", info, 0, 5, OptionalLong.empty());
    IntStream.range(0, 3).forEach(i -> exercise.addSubmissionResult(
        new SubmissionResult(i, 10, 0.0, SubmissionResult.Status.GRADED, exercise)));

    SubmissionViewModel submissionViewModel1 = new SubmissionViewModel(
        exercise, Collections.emptyList(), null, Collections.emptyMap(), "");

    assertEquals(4, submissionViewModel1.getCurrentSubmissionNumber());
    assertEquals("You are about to make submission 4 out of 5.",
        submissionViewModel1.getSubmissionCountText());
    assertNull(submissionViewModel1.getSubmissionWarning());

    exercise.addSubmissionResult(
        new SubmissionResult(3, 10, 0.0, SubmissionResult.Status.GRADED, exercise));
    SubmissionViewModel submissionViewModel2 = new SubmissionViewModel(
        exercise, Collections.emptyList(), null, Collections.emptyMap(), "");

    assertEquals("You are about to make submission 5 out of 5.",
        submissionViewModel2.getSubmissionCountText());
    assertNotNull(submissionViewModel2.getSubmissionWarning());

    SubmissionViewModel submissionViewModel3 = new SubmissionViewModel(
        exercise, Collections.emptyList(), null, Collections.emptyMap(), "");

    exercise.addSubmissionResult(
        new SubmissionResult(4, 10, 0.0, SubmissionResult.Status.GRADED, exercise));
    assertEquals("You are about to make submission 6 out of 5.",
        submissionViewModel3.getSubmissionCountText());
    assertNotNull(submissionViewModel3.getSubmissionWarning());

    // Max submissions 0
    SubmissionViewModel submissionViewModel4 = new SubmissionViewModel(
        new Exercise(0, "", "", info, 0, 0, OptionalLong.empty()),
        Collections.emptyList(), null, Collections.emptyMap(), "");
    assertEquals("You are about to make submission 1.",
        submissionViewModel4.getSubmissionCountText());
    assertNull(submissionViewModel4.getSubmissionWarning());
  }

  @Test
  public void testGetFiles() {
    SubmittableFile englishFile1 = new SubmittableFile("file1", "enFile1");
    SubmittableFile englishFile2 = new SubmittableFile("file2", "enFile2");
    SubmittableFile finnishFile1 = new SubmittableFile("file1", "fiFile1");
    SubmittableFile finnishFile2 = new SubmittableFile("file2", "fiFile2");
    Map<String, List<SubmittableFile>> files = new HashMap<>();
    files.put("en", List.of(englishFile1, englishFile2));
    files.put("fi", List.of(finnishFile1, finnishFile2));
    var exercise = new Exercise(
        324, "cool", "http://localhost:1324", new SubmissionInfo(files), 0, 0, OptionalLong.empty());

    SubmissionViewModel submission = new SubmissionViewModel(
        exercise, Collections.emptyList(), null, Collections.emptyMap(), "fi"
    );

    assertArrayEquals("getFiles returns the files corresponding to the given language",
        new SubmittableFile[]{finnishFile1, finnishFile2}, submission.getFiles());
  }

  @Test
  public void testDefaultGroup() {
    Exercise exercise = new Exercise(
        1000, "wow", "http://www.fi", new SubmissionInfo(Collections.emptyMap()), 0, 0, OptionalLong.empty());
    Group group = new Group(1, List.of("Jyrki", "Jorma"));
    List<Group> availableGroups = Collections.singletonList(group);

    SubmissionViewModel viewModel1 = new SubmissionViewModel(
        exercise, availableGroups, null, Collections.emptyMap(), "fi"
    );
    SubmissionViewModel viewModel2 = new SubmissionViewModel(
        exercise, availableGroups, group, Collections.emptyMap(), "fi"
    );

    assertNull(viewModel1.selectedGroup.get());
    assertNotNull(viewModel2.selectedGroup.get());

    assertFalse("The default group check box is unchecked when no default group exists",
        viewModel1.makeDefaultGroup.get());
    assertTrue("The default group check box is automatically checked if a default group exists",
        viewModel2.makeDefaultGroup.get());
  }
}
