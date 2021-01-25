package fi.aalto.cs.apluscourses.remoterobot;

import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.stepsProcessing.StepLogger;
import com.intellij.remoterobot.stepsProcessing.StepWorker;
import fi.aalto.cs.apluscourses.remoterobot.fixtures.DialogFixture;
import fi.aalto.cs.apluscourses.remoterobot.fixtures.NewProjectFixture;
import fi.aalto.cs.apluscourses.remoterobot.fixtures.WelcomeFrameFixture;
import java.time.Duration;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleTest {

  @BeforeClass
  public static void init() {
    StepWorker.registerProcessor(new StepLogger());
  }

  @Test
  public void testCreateNewProject() {
    RemoteRobot remoteRobot = new RemoteRobot("http://localhost:8580");
    step("Create new empty project", () -> {
      WelcomeFrameFixture welcomeFrame =
          remoteRobot.find(WelcomeFrameFixture.class, Duration.ofSeconds(10));
      welcomeFrame.newProjectButton().click();

      NewProjectFixture newProjectDialog = welcomeFrame.find(
          NewProjectFixture.class, DialogFixture.byTitle("New Project"), Duration.ofSeconds(20));
      newProjectDialog.findText("Empty Project").click();
      newProjectDialog.nextButton().click();
      newProjectDialog.finishButton().click();
    });
  }
}
