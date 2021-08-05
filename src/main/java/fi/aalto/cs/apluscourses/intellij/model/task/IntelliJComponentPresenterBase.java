package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import fi.aalto.cs.apluscourses.model.task.CancelHandler;
import fi.aalto.cs.apluscourses.model.task.ComponentPresenter;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import fi.aalto.cs.apluscourses.ui.ideactivities.GenericHighlighter;
import fi.aalto.cs.apluscourses.ui.ideactivities.OverlayPane;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public abstract class IntelliJComponentPresenterBase implements ComponentPresenter {

  public static final int REFRESH_INTERVAL = 1000;
  private final Timer timer;
  private final @NotNull String instruction;
  private final @NotNull String info;
  protected final @NotNull Project project;
  private OverlayPane overlayPane;
  private volatile CancelHandler cancelHandler; //NOSONAR
  private final AtomicBoolean highlighting = new AtomicBoolean(false);
  private final AtomicBoolean tryingToShow = new AtomicBoolean(false);

  protected IntelliJComponentPresenterBase(@NotNull String instruction,
                                           @NotNull String info,
                                           @NotNull Project project) {
    this.instruction = instruction;
    this.info = info;
    this.project = project;
    this.timer = new Timer();
  }

  @Override
  public void highlight() {
    ApplicationManager.getApplication()
        .invokeLater(this::highlightInternal, ModalityState.NON_MODAL);
    startTimer();
  }

  @RequiresEdt
  private void highlightInternal() {
    highlighting.set(true);
    if (overlayPane != null) {
      overlayPane.remove();
    }

    var highlighter = getHighlighter();
    var showing = false;
    if (highlighter == null && !tryingToShow.get() && tryToShow()) {
      showing = true; // tryToShow returned true
      highlighter = getHighlighter();
    }
    if (highlighter == null) {
      if (showing || tryingToShow.get()) {
        highlighting.set(false);
        tryingToShow.set(true);
        return; // tryToShow returned true, but component not visible yet
      } else {
        throw new IllegalStateException("Component was not found!");
      }
    } else {
      tryingToShow.set(false);
    }
    overlayPane = OverlayPane.installOverlay();
    overlayPane.addHighlighter(highlighter);
    overlayPane.clickEvent.addListener(cancelHandler, CancelHandler::onCancel);
    overlayPane.addPopup(highlighter.getComponent(), instruction, info);

    var progressButton = ComponentDatabase.getProgressButton();
    if (progressButton != null) {
      overlayPane.addHighlighter(new GenericHighlighter(progressButton));
    }
    highlighting.set(false);
  }

  @Override
  public void removeHighlight() {
    ApplicationManager.getApplication().invokeLater(this::removeHighlightInternal);
  }

  @RequiresEdt
  private void removeHighlightInternal() {
    if (overlayPane != null) {
      overlayPane.remove();
      overlayPane = null;
    }
    timer.cancel();
  }

  protected abstract GenericHighlighter getHighlighter();

  protected abstract boolean tryToShow();

  public boolean isVisible() {
    var highlighter = getHighlighter();
    return highlighter != null && highlighter.getComponent().isVisible();
  }

  @Override
  public void setCancelHandler(CancelHandler cancelHandler) {
    this.cancelHandler = cancelHandler;
  }

  private void startTimer() {
    timer.scheduleAtFixedRate(new TaskRefresher(), REFRESH_INTERVAL, REFRESH_INTERVAL);
  }

  private class TaskRefresher extends TimerTask {
    @Override
    public void run() {
      ApplicationManager.getApplication()
          .invokeLater(() -> {
            var progressButtonHighlighted = overlayPane != null
                && overlayPane.hasHighlighterForComponent(ComponentDatabase.getProgressButton());
            if (!highlighting.get()
                && (tryingToShow.get()
                || !isVisible()
                || !progressButtonHighlighted)) {
              highlightInternal();
            }
          }, ModalityState.NON_MODAL);
    }
  }
}
