package fi.aalto.cs.apluscourses.presentation.exercise;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubmissionViewModelTest {

  @Test
  void testValidation() {
    Group group = new Group(200, Collections.singletonList(new Group.GroupMember(1, "Someone")));
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
        100, "Exercise", "http://localhost:1000", submissionInfo, 0, 0, OptionalLong.empty(), null, false);

    SubmissionViewModel submissionViewModel =
        new SubmissionViewModel(exercise, groups, null, fileMap, language);

    Assertions.assertNotNull(submissionViewModel.selectedGroup.validate(),
        "The validation should fail when no group is yet selected");
  }

  @Test
  void testSubmissionNumbers() {
    var info = new SubmissionInfo(Collections.emptyMap());
    Exercise exercise = new Exercise(1, "ex", "http://localhost:2000", info, 0, 5, OptionalLong.empty(), null, false);
    IntStream.range(0, 3).forEach(i -> exercise.addSubmissionResult(
        new SubmissionResult(i, 10, 0.0, SubmissionResult.Status.GRADED, exercise)));

    SubmissionViewModel submissionViewModel1 = new SubmissionViewModel(
        exercise, Collections.emptyList(), null, Collections.emptyMap(), "");

    Assertions.assertEquals(4, submissionViewModel1.getCurrentSubmissionNumber());
    Assertions.assertEquals("You are about to make submission 4 out of 5.",
        submissionViewModel1.getSubmissionCountText());
    var project = mock(Project.class);
    Assertions.assertNull(submissionViewModel1.getSubmissionWarning(project));

    exercise.addSubmissionResult(
        new SubmissionResult(3, 10, 0.0, SubmissionResult.Status.GRADED, exercise));
    SubmissionViewModel submissionViewModel2 = new SubmissionViewModel(
        exercise, Collections.emptyList(), null, Collections.emptyMap(), "");

    Assertions.assertEquals("You are about to make submission 5 out of 5.",
        submissionViewModel2.getSubmissionCountText());
    Assertions.assertNotNull(submissionViewModel2.getSubmissionWarning(project));

    SubmissionViewModel submissionViewModel3 = new SubmissionViewModel(
        exercise, Collections.emptyList(), null, Collections.emptyMap(), "");

    exercise.addSubmissionResult(
        new SubmissionResult(4, 10, 0.0, SubmissionResult.Status.GRADED, exercise));
    Assertions.assertEquals("You are about to make submission 6 out of 5.",
        submissionViewModel3.getSubmissionCountText());
    Assertions.assertNotNull(submissionViewModel3.getSubmissionWarning(project));

    // Max submissions 0
    SubmissionViewModel submissionViewModel4 = new SubmissionViewModel(
        new Exercise(0, "", "", info, 0, 0, OptionalLong.empty(), null, false),
        Collections.emptyList(), null, Collections.emptyMap(), "");
    Assertions.assertEquals("You are about to make submission 1.", submissionViewModel4.getSubmissionCountText());
    Assertions.assertNull(submissionViewModel4.getSubmissionWarning(project));
  }

  @Test
  void testGetFiles() {
    SubmittableFile englishFile1 = new SubmittableFile("file1", "enFile1");
    SubmittableFile englishFile2 = new SubmittableFile("file2", "enFile2");
    SubmittableFile finnishFile1 = new SubmittableFile("file1", "fiFile1");
    SubmittableFile finnishFile2 = new SubmittableFile("file2", "fiFile2");
    Map<String, List<SubmittableFile>> files = new HashMap<>();
    files.put("en", List.of(englishFile1, englishFile2));
    files.put("fi", List.of(finnishFile1, finnishFile2));
    var exercise = new Exercise(
        324, "cool", "http://localhost:1324", new SubmissionInfo(files), 0, 0,
        OptionalLong.empty(), null, false);

    SubmissionViewModel submission = new SubmissionViewModel(
        exercise, Collections.emptyList(), null, Collections.emptyMap(), "fi"
    );

    Assertions.assertArrayEquals(new SubmittableFile[] {finnishFile1, finnishFile2}, submission.getFiles(),
        "getFiles returns the files corresponding to the given language");
  }

  @Test
  void testDefaultGroup() {
    Exercise exercise = new Exercise(
        1000, "wow", "http://www.fi", new SubmissionInfo(Collections.emptyMap()), 0, 0,
        OptionalLong.empty(), null, false);
    Group group = new Group(1, List.of(
        new Group.GroupMember(1, "Jyrki"),
        new Group.GroupMember(1, "Jorma")));
    List<Group> availableGroups = Collections.singletonList(group);

    SubmissionViewModel viewModel1 = new SubmissionViewModel(
        exercise, availableGroups, null, Collections.emptyMap(), "fi"
    );
    SubmissionViewModel viewModel2 = new SubmissionViewModel(
        exercise, availableGroups, group, Collections.emptyMap(), "fi"
    );

    Assertions.assertNull(viewModel1.selectedGroup.get());
    Assertions.assertNotNull(viewModel2.selectedGroup.get());

    Assertions.assertFalse(viewModel1.makeDefaultGroup.get(),
        "The default group check box is unchecked when no default group exists");
    Assertions.assertTrue(viewModel2.makeDefaultGroup.get(),
        "The default group check box is automatically checked if a default group exists");
  }
}
