package fi.aalto.cs.apluscourses.intellij.model;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.NewModulesVersionsNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.model.ModelExtensions;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.BuildInfo;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.Version;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class CourseUpdaterTest {

  private CourseUpdater updater;
  private String json;
  private Module module;
  private URL courseUrl;
  private Project project;
  private CourseUpdater.CourseConfigurationFetcher configurationFetcher;
  private Event event;
  private Notifier notifier;

  /**
   * Set up mock objects before each test.
   */
  @BeforeEach
  public void setUp() throws IOException {
    module = mock(Module.class);
    doReturn("Awesome Module").when(module).getName();
    doReturn(new URL("http://example.org")).when(module).getUrl();
    courseUrl = new URL("http://localhost:8009");
    project = mock(Project.class);
    configurationFetcher = mock(CourseUpdater.CourseConfigurationFetcher.class);
    json = "{\"modules\":[{\"name\":\"Module\",\"url\":\"http://example.org\",\"id\":\"a\"}]}";
    var bytes = json.getBytes(StandardCharsets.UTF_8);
    doAnswer(invocationOnMock -> new ByteArrayInputStream(bytes))
            .when(configurationFetcher)
            .fetch(any(URL.class));
    event = mock(Event.class);
    notifier = mock(Notifier.class);
    var course = new ModelExtensions.TestCourse(
            "1", "O1", "http://example.com", Collections.emptyList(), Collections.singletonList(module),
            Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyList(), Collections.emptyMap(), BuildInfo.INSTANCE.courseVersion,
            Collections.emptyMap()
    );
    updater = new CourseUpdater(
            mock(CourseProject.class), course, project, courseUrl, configurationFetcher, event, notifier, 50L
    );
  }

  @Test
  public void testCourseUpdaterIsInterruptible() {
    updater.restart();
    updater.stop();
    verifyNoInteractions(event);
    verifyNoInteractions(notifier);
    verifyNoInteractions(project);
  }

  @Disabled("Uses PluginSettings")
  @Test
  public void testCourseUpdaterWithNoUpdatableModules() throws IOException, InterruptedException {
    updater.restart();
    Thread.sleep(300L);
    updater.stop();
    verify(module, atLeast(2)).updateVersion(eq(new Version(1, 0)));
    verify(configurationFetcher, atLeast(2)).fetch(eq(courseUrl));
    verify(event, atLeast(2)).trigger();
    verifyNoInteractions(notifier);
    verifyNoInteractions(project);
  }

  @Disabled("Uses PluginSettings")
  @Test
  public void testCourseUpdaterNotifies() throws IOException, InterruptedException {
    doReturn(true).when(module).isUpdatable();
    updater.restart();
    Thread.sleep(300L);
    updater.stop();
    verify(configurationFetcher, atLeast(2)).fetch(eq(courseUrl));
    verify(event, atLeast(2)).trigger();
    var argumentCaptor = ArgumentCaptor.forClass(NewModulesVersionsNotification.class);
    verify(notifier, times(1)).notifyAndHide(argumentCaptor.capture(), eq(project));
    assertThat(argumentCaptor.getValue().getContent(), containsString("Awesome Module"));
    verifyNoInteractions(project);
  }

}
