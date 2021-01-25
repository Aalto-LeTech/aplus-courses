package fi.aalto.cs.apluscourses.remoterobot.fixtures;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import org.jetbrains.annotations.NotNull;

public class NewProjectFixture extends DialogFixture {
  public NewProjectFixture(@NotNull RemoteRobot remoteRobot,
                           @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public ButtonFixture nextButton() {
    return find(ButtonFixture.class, ButtonFixture.byAccessibleName("Next"));
  }

  public ButtonFixture finishButton() {
    return find(ButtonFixture.class, ButtonFixture.byAccessibleName("Finish"));
  }
}
