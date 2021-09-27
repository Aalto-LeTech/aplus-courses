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
import javax.swing.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IntelliJComponentPresenterBase implements ComponentPresenter {

  public static final int REFRESH_INTERVAL = 1000;
  private final Timer timer;
  private final @Nullable String instruction;
  private final @Nullable String info;
  protected final @NotNull Project project;
  private final @NotNull Action @NotNull [] actions;
  private OverlayPane overlayPane;
  private volatile CancelHandler cancelHandler; //NOSONAR
  private boolean tryingToShow = false;

  /**
   * Constructor for the presenter.
   *
   * @param instruction The heading text of the balloon popup. If null, the popup won't be shown.
   * @param info The info text of the balloon popup. If null, the text will be empty.
   */
  protected IntelliJComponentPresenterBase(@Nullable String instruction,
                                           @Nullable String info,
                                           @NotNull Project project,
                                           @NotNull Action @NotNull [] actions) {
    this.instruction = instruction;
    this.info = info;
    this.project = project;
    this.actions = actions;
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
    if (overlayPane == null) {
      overlayPane = OverlayPane.installOverlay();
    }
    overlayPane.clickEvent.addListener(cancelHandler, CancelHandler::onCancel);

    var progressButton = ComponentDatabase.getProgressButton();
    if (progressButton != null) {
      overlayPane.addHighlighter(new GenericHighlighter(progressButton));
    }

    var highlighter = getHighlighter();

    if (highlighter != null && isVisible()) {
      tryingToShow = false;
    } else if (tryingToShow) {
      return;
    } else if (tryToShow()) {
      highlighter = getHighlighter();
      if (highlighter == null) {
        tryingToShow = true;
        return;
      }
    } else {
      throw new IllegalStateException("Component was not found!");
    }

    overlayPane.addHighlighter(highlighter);
    if (instruction != null) {
      overlayPane.addPopup(highlighter.getComponent(), instruction, info == null ? "" : info, actions);
    }
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
            var progressButton = ComponentDatabase.getProgressButton();
            var progressButtonHighlighted = overlayPane != null
                && progressButton != null
                && overlayPane.hasHighlighterForComponent(progressButton);
            if (!isVisible() || !progressButtonHighlighted || tryingToShow) {
              highlightInternal();
            }
          }, ModalityState.NON_MODAL);
    }
  }
}
