package fi.aalto.cs.apluscourses.remoterobot.fixtures;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.search.locators.Locator;
import org.jetbrains.annotations.NotNull;

@FixtureName(name = "Button")
public class ButtonFixture extends ComponentFixture {
  public ButtonFixture(@NotNull RemoteRobot remoteRobot,
                       @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public static Locator byAccessibleName(@NotNull String accessibleName) {
    return byXpath("//div[@accessiblename='" + accessibleName + "' and @class='JButton']");
  }

  public void click() {
    runJs("robot.click(component, new Point(component.getWidth() / 2, component.getHeight() / 2),"
        + "MouseButton.LEFT_BUTTON, 1);");
  }
}
