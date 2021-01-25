package fi.aalto.cs.apluscourses.remoterobot.fixtures;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import com.intellij.remoterobot.search.locators.Locator;
import org.jetbrains.annotations.NotNull;

public class DialogFixture extends ContainerFixture {
  public DialogFixture(@NotNull RemoteRobot remoteRobot,
                       @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public static Locator byTitle(@NotNull String title) {
    return byXpath("title " + title, "//div[@title='" + title + "' and @class='MyDialog']");
  }
}
