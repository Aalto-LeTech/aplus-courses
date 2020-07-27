package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class SubmissionTest {

  @Test
  public void testCreate() {
    Exercise exercise = new Exercise(85678, "ex");
    SubmittableFile fileA = new SubmittableFile("keyA", "fileA");
    SubmittableFile fileB = new SubmittableFile("keyB", "fileB");
    SubmissionInfo info = new SubmissionInfo(10, new SubmittableFile[] { fileA, fileB });
    Map<String, Path> files = new HashMap<>();
    files.put("fileA", Paths.get("some.file"));
    files.put("fileB", Paths.get("other.file"));
    Group group = new Group(0, Collections.singletonList("Only me"));

    Submission submission = new Submission(exercise, info, files, group);

    assertSame(exercise, submission.getExercise());
    assertSame(info, submission.getSubmissionInfo());
    assertThat(files, is(files));
    assertSame(group, submission.getGroup());
  }
}
