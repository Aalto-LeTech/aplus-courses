package fi.aalto.cs.apluscourses.intellij.services;

import static com.intellij.util.io.NettyKt.getOrigin;
import static java.lang.Long.parseLong;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import fi.aalto.cs.apluscourses.intellij.utils.SubmissionDownloader;
import fi.aalto.cs.apluscourses.utils.cache.CachePreferences;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.io.IOException;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.RestService;

public class OpenSubmissionService extends RestService {
  private static final String NOT_LOADED_ERROR = "A+ Courses hasn't loaded assignments yet";

  @Nullable
  @Override
  public String execute(@NotNull QueryStringDecoder urlDecoder, @NotNull FullHttpRequest request,
                        @NotNull ChannelHandlerContext context) throws IOException {
    var apiRequest = new OpenSubmissionRequest(getStringParameter("exerciseUrl", urlDecoder),
        getStringParameter("submissionId", urlDecoder));
    var error = openSubmission(apiRequest);
    if (error != null) {
      return error;
    }
    sendOk(request, context);

    return null;
  }

  @NotNull
  @Override
  protected String getServiceName() {
    return "aPlusSubmission";
  }

  @Override
  protected boolean isMethodSupported(@NotNull HttpMethod method) {
    return method == HttpMethod.GET;
  }

  @NotNull
  @Override
  protected OriginCheckResult isOriginAllowed(@NotNull HttpRequest request) {
    var project = getOpenAPlusProject();
    if (project == null) {
      return OriginCheckResult.FORBID;
    }
    var courseProject = PluginSettings.getInstance().getCourseProject(project);
    if (courseProject == null) {
      return OriginCheckResult.FORBID;
    }
    var apiUrl = courseProject.getCourse().getHtmlUrl();
    return apiUrl.equals(getOrigin(request) + "/") ? OriginCheckResult.ALLOW : OriginCheckResult.FORBID;
  }

  @Nullable
  private String openSubmission(@NotNull OpenSubmissionRequest request) {
    if (request.submissionId == null || request.exerciseUrl == null) {
      return "HTTP request missing parameters";
    }

    var project = getOpenAPlusProject();
    if (project == null) {
      return "No A+ Courses project found";
    }
    var courseProject = PluginSettings.getInstance().getCourseProject(project);
    if (courseProject == null) {
      return NOT_LOADED_ERROR;
    }
    var course = courseProject.getCourse();
    var dataSource = course.getExerciseDataSource();
    try {
      var exerciseTree = courseProject.getExerciseTree();
      if (exerciseTree == null) {
        return NOT_LOADED_ERROR;
      }
      var exercise = exerciseTree.findExerciseByUrl(request.exerciseUrl);
      if (exercise == null) {
        return "Exercise not found";
      }
      var auth = courseProject.getAuthentication();
      if (auth == null) {
        return "A+ Courses Project not authenticated";
      }

      var submissionResult = dataSource.getSubmissionResult(
          course.getApiUrl() + "submissions/" + request.submissionId + "/", exercise,
          courseProject.getAuthentication(), CachePreferences.FOR_THIS_SESSION_ONLY);
      new SubmissionDownloader().downloadSubmission(project, course, exercise, submissionResult);
    } catch (IOException e) {
      return "Error while downloading submission";
    }
    return null;
  }

  @Nullable
  private Project getOpenAPlusProject() {
    var projects = Arrays.stream(ProjectManager.getInstance().getOpenProjects())
        .filter(project -> PluginSettings.getInstance().getCourseProject(project) != null);
    var optProject = projects.findFirst();
    if (optProject.isEmpty()) {
      return null;
    }
    return optProject.get();
  }

  private static class OpenSubmissionRequest {
    @Nullable
    private final String exerciseUrl;
    @Nullable
    private final Long submissionId;

    public OpenSubmissionRequest(@Nullable String exerciseUrl, @Nullable String submissionId) {
      this.exerciseUrl = exerciseUrl;
      this.submissionId = submissionId != null ? parseLong(submissionId) : null;
    }
  }
}
