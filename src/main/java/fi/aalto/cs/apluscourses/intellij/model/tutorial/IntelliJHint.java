package fi.aalto.cs.apluscourses.intellij.model.tutorial;

import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJTutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.Hint;
import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import fi.aalto.cs.apluscourses.ui.tutorials.NormalBalloon;
import fi.aalto.cs.apluscourses.ui.tutorials.OverlayPane;
import java.awt.event.ActionEvent;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJHint extends Hint implements IntelliJTutorialClientObject {
  private final @NotNull SceneControl sceneControl;
  private final @NotNull OverlayPane overlayPane;
  private @Nullable NormalBalloon balloon;

  public IntelliJHint(@NotNull String content,
                      @Nullable String title,
                      @Nullable SceneSwitch sceneSwitch,
                      @NotNull IntelliJTutorialComponent<?> component,
                      @NotNull OverlayPane overlayPane) {
    super(title, content, component);
    this.sceneControl = sceneSwitch == null ? NullSceneControl.INSTANCE : new SceneControlImpl(sceneSwitch);
    this.overlayPane = overlayPane;
  }

  @Override
  public void activate() {
    balloon = new NormalBalloon(
      getIntelliJComponent(),
      Optional.ofNullable(getTitle()).orElse(""),
      getContent(),
      sceneControl.getActions()
    );
    overlayPane.addBalloon(balloon);
    balloon.init();
  }

  @Override
  public void deactivate() {
    if (balloon != null) {
      overlayPane.removeBalloon(balloon);
      balloon.release();
      balloon = null;
    }
  }

  private interface SceneControl {
    @NotNull Action @NotNull[] getActions();
  }

  private static class NullSceneControl implements SceneControl {
    static final NullSceneControl INSTANCE = new NullSceneControl();

    @Override
    public @NotNull Action @NotNull [] getActions() {
      return new Action[0];
    }
  }

  private static class SceneControlImpl implements SceneControl {
    private final @NotNull SceneSwitch sceneSwitch;
    private final @NotNull Action @NotNull[] actions = new Action[] {
        new BackwardAction(),
        new ForwardAction()
    };

    private SceneControlImpl(@NotNull SceneSwitch sceneSwitch) {
      this.sceneSwitch = sceneSwitch;
    }

    @Override
    public @NotNull Action @NotNull [] getActions() {
      return actions;
    }

    private class ForwardAction extends AbstractAction {
      public ForwardAction() {
        super("Next");
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        sceneSwitch.goForward();
      }

      @Override
      public boolean isEnabled() {
        return sceneSwitch.canGoForward();
      }
    }

    private class BackwardAction extends AbstractAction {
      public BackwardAction() {
        super("Back");
      }

      @Override
      public void actionPerformed(ActionEvent e) {
        sceneSwitch.goBackward();
      }

      @Override
      public boolean isEnabled() {
        return sceneSwitch.canGoBackward();
      }
    }
  }
}
