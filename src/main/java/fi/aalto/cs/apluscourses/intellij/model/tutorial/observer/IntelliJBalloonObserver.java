package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.ui.BalloonImpl;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJTutorialComponent;
import fi.aalto.cs.apluscourses.intellij.utils.IntelliJUIUtil;
import org.jetbrains.annotations.NotNull;

public class IntelliJBalloonObserver extends IntelliJScheduledObserverBase {

  public IntelliJBalloonObserver(@NotNull IntelliJTutorialComponent<?> component) {
    super(component);
  }

  @Override
  protected boolean check() {
    return IntelliJUIUtil.getBalloon(getProject()) != null;
  }
}
