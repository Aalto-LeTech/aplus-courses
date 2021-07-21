package fi.aalto.cs.apluscourses.model.task;

import java.util.Timer;

public interface ComponentPresenter {

  void highlight();

  void removeHighlight();

  boolean isVisible();

  Timer getTimer();
}
