package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class Submission {

  private final SubmittableExercise exercise;
  private final Path[] filePaths;
  @NotNull
  private final APlusAuthentication authentication;
  private final Group group;

  /**
   * Constructs a new object instance.
   *
   * @param exercise Exercise.
   * @param filePaths Array of paths.
   * @param authentication API authentication.
   * @param group Group in which the submission is made.
   */
  public Submission(@NotNull SubmittableExercise exercise,
                    @NotNull Path[] filePaths,
                    @NotNull APlusAuthentication authentication,
                    @NotNull Group group) {
    this.exercise = exercise;
    this.filePaths = filePaths;
    this.authentication = authentication;
    this.group = group;
  }

  public void submit() throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("_aplus_group", group.getId());
    for (Path path : filePaths) {
      data.put(path.getFileName().toString(), path.toFile());
    }
    CoursesClient.post(new URL(PluginSettings.A_PLUS_API_BASE_URL + "/exercises/" + exercise.getId()
        + "/submissions/submit/"), authentication, data);
  }
}
