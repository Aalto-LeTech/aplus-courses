package fi.aalto.cs.apluscourses.intellij.services;

import static com.intellij.util.io.NettyKt.getOrigin;
import static java.lang.Long.parseLong;

import com.intellij.openapi.application.ApplicationManager;
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
  private final CourseProjectProvider courseProjectProvider;

  public OpenSubmissionService() {
    this(PluginSettings.getInstance()::getCourseProject);
  }

  public OpenSubmissionService(@NotNull CourseProjectProvider courseProjectProvider) {
    this.courseProjectProvider = courseProjectProvider;
  }

  @Nullable
  @Override
  public String execute(@NotNull QueryStringDecoder urlDecoder, @NotNull FullHttpRequest request,
                        @NotNull ChannelHandlerContext context) throws IOException {
    if (!isOriginApi(request)) {
      return "Origin not allowed";
    }
    var submissionIdString = getStringParameter("submissionId", urlDecoder);
    var exerciseUrl = getStringParameter("exerciseUrl", urlDecoder);
    if (submissionIdString == null || exerciseUrl == null) {
      return "HTTP request missing parameters";
    }
    long submissionId = parseLong(submissionIdString);
    var error = openSubmission(submissionId, exerciseUrl);
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
    return method == HttpMethod.POST;
  }

  @NotNull
  @Override
  protected OriginCheckResult isOriginAllowed(@NotNull HttpRequest request) {
    return OriginCheckResult.ALLOW;
  }

  private boolean isOriginApi(@NotNull HttpRequest request) {
    var project = getOpenAPlusProject();
    if (project == null) {
      return false;
    }
    var courseProject = courseProjectProvider.getCourseProject(project);
    if (courseProject == null) {
      return false;
    }
    var apiUrl = courseProject.getCourse().getHtmlUrl();
    return apiUrl.equals(getOrigin(request) + "/");
  }

  @Nullable
  private String openSubmission(long submissionId, @NotNull String exerciseUrl) {
    var project = getOpenAPlusProject();
    if (project == null) {
      return "No A+ Courses project found";
    }
    var courseProject = courseProjectProvider.getCourseProject(project);
    if (courseProject == null) {
      return NOT_LOADED_ERROR;
    }
    var course = courseProject.getCourse();
    var dataSource = course.getExerciseDataSource();

    var exerciseTree = courseProject.getExerciseTree();
    if (exerciseTree == null) {
      return NOT_LOADED_ERROR;
    }
    var exercise = exerciseTree.findExerciseByUrl(exerciseUrl);
    if (exercise == null) {
      return "Exercise not found";
    }
    var auth = courseProject.getAuthentication();
    if (auth == null) {
      return "A+ Courses Project not authenticated";
    }

    try {
      var submissionResult = dataSource.getSubmissionResult(
          course.getApiUrl() + "submissions/" + submissionId + "/", exercise,
          courseProject.getAuthentication(), CachePreferences.FOR_THIS_SESSION_ONLY);

      ApplicationManager.getApplication().invokeLater(
          () -> new SubmissionDownloader().downloadSubmission(project, course, exercise, submissionResult));
    } catch (IOException e) {
      return "Error while downloading submission";
    }
    return null;
  }

  @Nullable
  private Project getOpenAPlusProject() {
    return Arrays.stream(ProjectManager.getInstance().getOpenProjects())
        .filter(project -> courseProjectProvider.getCourseProject(project) != null)
        .findFirst()
        .orElse(null);
  }
}
