package fi.aalto.cs.apluscourses.model.task;

import java.util.Timer;

public interface ComponentPresenter {

  void highlight();

  void removeHighlight();

  void setCancelHandler(CancelHandler cancelHandler);
}
