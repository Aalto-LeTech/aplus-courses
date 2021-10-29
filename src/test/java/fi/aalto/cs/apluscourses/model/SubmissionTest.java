package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

public class SubmissionTest {

  @Test
  public void testCreate() {
    var fileA = new SubmittableFile("keyA", "fileA");
    var fileB = new SubmittableFile("keyB", "fileB");
    var language = "de";
    var info = new SubmissionInfo(Collections.singletonMap(language, List.of(fileA, fileB)));
    Exercise exercise = new Exercise(85678, "ex", "http://localhost:1000", info, 0, 0, OptionalLong.empty());
    Map<String, Path> files = new HashMap<>();
    files.put("fileA", Paths.get("some.file"));
    files.put("fileB", Paths.get("other.file"));
    Group group = new Group(0, Collections.singletonList("Only me"));

    Submission submission = new Submission(exercise, files, group, language);

    assertSame(exercise, submission.getExercise());
    assertThat(files, is(files));
    assertSame(group, submission.getGroup());
    assertEquals(language, submission.getLanguage());
  }
}
