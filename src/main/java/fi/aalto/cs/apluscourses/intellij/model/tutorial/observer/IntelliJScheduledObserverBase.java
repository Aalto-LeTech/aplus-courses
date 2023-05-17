package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import fi.aalto.cs.apluscourses.intellij.model.tutorial.IntelliJTutorialClientObject;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJTutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.Observer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.jetbrains.annotations.NotNull;

public abstract class IntelliJScheduledObserverBase extends Observer implements IntelliJTutorialClientObject {
  private static final int DEFAULT_INTERVAL_MILLIS = 100;

  private final @NotNull Timer timer;

  public IntelliJScheduledObserverBase(@NotNull IntelliJTutorialComponent<?> component) {
    this(DEFAULT_INTERVAL_MILLIS, component);
  }

  public IntelliJScheduledObserverBase(int intervalMillis, @NotNull IntelliJTutorialComponent<?> component) {
    super(component);
    timer = new Timer(intervalMillis, new MyListener());
  }

  @Override
  public void activate() {
    timer.start();
  }

  @Override
  public void deactivate() {
    timer.stop();
  }

  protected abstract boolean check();

  private class MyListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (check()) {
        fire();
      }
    }
  }
}
