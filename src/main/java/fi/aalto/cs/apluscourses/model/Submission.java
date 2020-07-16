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

  private final Exercise exercise;
  private final SubmissionInfo submissionInfo;
  private final Path[] filePaths;
  @NotNull
  private final APlusAuthentication authentication;
  private final Group group;
  @NotNull
  private final Submitter submitter;

  /**
   * Constructs a new object instance.
   *
   * @param exercise Exercise.
   * @param filePaths Array of paths.
   * @param authentication API authentication.
   * @param group Group in which the submission is made.
   */
  public Submission(@NotNull Exercise exercise,
                    @NotNull SubmissionInfo submissionInfo,
                    @NotNull Path[] filePaths,
                    @NotNull APlusAuthentication authentication,
                    @NotNull Group group,
                    @NotNull Submitter submitter) {
    this.exercise = exercise;
    this.submissionInfo = submissionInfo;
    this.filePaths = filePaths;
    this.authentication = authentication;
    this.group = group;
    this.submitter = submitter;
  }

  /**
   * Sends the submission to the server.
   *
   * @throws IOException If there are IO related errors.
   */
  public void submit() throws IOException {
    Map<String, Object> data = new HashMap<>();
    data.put("_aplus_group", group.getId());
    for (Path path : filePaths) {
      data.put(path.getFileName().toString(), path.toFile());
    }
    submitter.submit(new URL(PluginSettings.A_PLUS_API_BASE_URL + "/exercises/" + exercise.getId()
        + "/submissions/submit/"), authentication, data);
  }

  @FunctionalInterface
  public interface Submitter {
    void submit(URL url, CoursesClient.Authentication authentication, Map<String, Object> data)
        throws IOException;
  }
}
