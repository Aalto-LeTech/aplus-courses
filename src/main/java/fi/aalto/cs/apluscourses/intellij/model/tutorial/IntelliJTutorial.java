package fi.aalto.cs.apluscourses.intellij.model.tutorial;

import fi.aalto.cs.apluscourses.model.tutorial.Highlight;
import fi.aalto.cs.apluscourses.model.tutorial.Tutorial;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialClientObject;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialState;
import fi.aalto.cs.apluscourses.ui.tutorials.OverlayPane;
import icons.PluginIcons;
import java.awt.Color;
import java.util.Collection;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class IntelliJTutorial extends Tutorial {
  private final OverlayPane overlayPane;

  public IntelliJTutorial(@NotNull Collection<@NotNull TutorialState> states,
                          @NotNull Collection<@NotNull TutorialClientObject> objects,
                          @NotNull String initialStateKey,
                          @NotNull OverlayPane overlayPane) {
    super(states, objects, initialStateKey);
    this.overlayPane = overlayPane;
  }

  @Override
  public void activate() {
    overlayPane.init();
    overlayPane.defineHighlighting(
        Highlight.Degree.normal,
        new Color(PluginIcons.ACCENT_COLOR).brighter(), 0.2f,
        Color.BLACK, 0.7f, true);
    overlayPane.defineHighlighting(Highlight.Degree.focus,
        Color.YELLOW.brighter().brighter(), 0.3f,
        Color.YELLOW.brighter().brighter(), 0.1f, false);
    super.activate();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    overlayPane.release();
  }
}
