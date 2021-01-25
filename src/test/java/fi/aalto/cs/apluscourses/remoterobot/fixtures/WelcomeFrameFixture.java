package fi.aalto.cs.apluscourses.remoterobot.fixtures;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import org.jetbrains.annotations.NotNull;

@DefaultXpath(by = "FlatWelcomeFrame type", xpath = "//div[@class='FlatWelcomeFrame']")
@FixtureName(name = "Welcome Frame")
public class WelcomeFrameFixture extends ContainerFixture {

  public WelcomeFrameFixture(@NotNull RemoteRobot remoteRobot,
                             @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public ButtonFixture newProjectButton() {
    return find(ButtonFixture.class, ButtonFixture.byAccessibleName("New Project"));
  }
}
