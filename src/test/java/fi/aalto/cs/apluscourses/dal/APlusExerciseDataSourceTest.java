package fi.aalto.cs.apluscourses.dal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.Submission;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class APlusExerciseDataSourceTest {

  final Authentication authentication = mock(Authentication.class);
  final String url = "https://example.com/";

  Client client;
  Parser parser;
  ExerciseDataSource exerciseDataSource;

  /**
   * Called before each test.
   */
  @BeforeEach
  void setUp() {
    client = mock(Client.class);
    parser = mock(Parser.class);
    doCallRealMethod().when(parser).parseArray(any(), any());
    exerciseDataSource = new APlusExerciseDataSource(url, client, parser);
  }

  @Test
  void testDefaultConstructor() {
    var exerciseDataSource = new APlusExerciseDataSource(
        url, Paths.get(FileUtilRt.getTempDirectory()));
    Assertions.assertEquals(url, exerciseDataSource.getApiUrl());
    Assertions.assertTrue(exerciseDataSource.getClient() instanceof APlusExerciseDataSource.DefaultDataAccess);
    Assertions.assertTrue(exerciseDataSource.getParser() instanceof APlusExerciseDataSource.DefaultDataAccess);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSubmit() throws IOException {
    String key0 = "firstKey";
    String key1 = "anotherKey";
    SubmittableFile subFile0 = new SubmittableFile(key0, "first.scala");
    SubmittableFile subFile1 = new SubmittableFile(key1, "another.scala");

    Path path0 = Paths.get("somePath");
    Path path1 = Paths.get("anotherPath");
    Map<String, Path> paths = new HashMap<>();
    paths.put(key0, path0);
    paths.put(key1, path1);

    var info = new SubmissionInfo(Collections.singletonMap("fi", List.of(subFile0, subFile1)));

    Exercise exercise = new Exercise(71, "newex", "https://example.com", info, 0, 0,
        OptionalLong.empty(), null, false);

    Group group = new Group(435, new ArrayList<>());

    Submission submission = new Submission(exercise, paths, group, "fi");

    exerciseDataSource.submit(submission, authentication);

    ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
    verify(client).post(
        eq("https://example.com/exercises/71/submissions/submit/"),
        eq(authentication),
        dataCaptor.capture()
    );

    Map<String, Object> data = dataCaptor.getValue();
    Assertions.assertEquals(3, data.size());

    Assertions.assertEquals(path0, ((File) data.get(key0)).toPath());
    Assertions.assertEquals(path1, ((File) data.get(key1)).toPath());

    String aplusArg = (String) data.get("__aplus__");
    JSONObject object = new JSONObject(new JSONTokener(aplusArg));
    Assertions.assertEquals(2, object.length());
    Assertions.assertEquals(435, object.getInt("group"));
    Assertions.assertEquals("fi", object.getString("lang"));
  }
}
