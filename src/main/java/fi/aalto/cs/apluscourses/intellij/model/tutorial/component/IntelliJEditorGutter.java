package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.tutorial.LineRange;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.utils.Cast;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJEditorGutter extends IntelliJTutorialComponent<JComponent> implements IntelliJEditorDescendant {
  public static final @NotNull String RUN = "run";
  private final @NotNull LineRange lineRange;
  private final @NotNull Icon expectedIcon;

  public IntelliJEditorGutter(@NotNull LineRange lineRange,
                              @NotNull String command,
                              @Nullable TutorialComponent parent,
                              @Nullable Project project) {
    super(parent, project);
    this.lineRange = lineRange;
    expectedIcon = getIcon(command);
  }

  @Override
  protected @Nullable Rectangle getBounds(@NotNull JComponent component) {
    EditorGutterComponentEx gutter;
    Editor editor;
    GutterIconRenderer renderer;
    Point center;
    if ((editor = getEditor()) == null
        || (gutter = getGutter(editor)) == null
        || (renderer = getRenderer(gutter)) == null
        || (center = gutter.getCenterPoint(renderer)) == null) {
      return new Rectangle();
    }
    var icon = renderer.getIcon();
    var w = icon.getIconWidth();
    var h = icon.getIconHeight();
    var rect = component.getVisibleRect().createIntersection(
        new Rectangle(center.x - w / 2, center.y - h / 2, w, h)
      ).getBounds();
    return SwingUtilities.convertRectangle(component, rect, component.getParent());
  }

  private static @NotNull Icon getIcon(@NotNull String type) {
    switch (type) {
      case RUN:
        return AllIcons.RunConfigurations.TestState.Run;
      default:
        throw new IllegalArgumentException("Unknown gutter type: " + type);
    }
  }

  private static @Nullable EditorGutterComponentEx getGutter(@NotNull Editor editor) {
    return Optional.of(editor.getGutter())
        .map(Cast.to(EditorGutterComponentEx.class)::orNull)
        .orElse(null);
  }

  private @Nullable GutterIconRenderer getRenderer(@NotNull EditorGutterComponentEx gutter) {
    return lineRange.stream()
        .map(i -> i - 1)
        .mapToObj(gutter::getGutterRenderers)
        .flatMap(List::stream)
        .filter(this::doesGutterMarkMatch)
        .findFirst()
        .map(Cast.to(GutterIconRenderer.class)::orNull)
        .orElse(null);
  }

  private boolean doesGutterMarkMatch(@NotNull GutterMark gutterMark) {
    return gutterMark.getIcon() == expectedIcon;
  }

  @Override
  protected @Nullable JComponent getAwtComponent() {
    return Optional.ofNullable(getEditor())
        .map(IntelliJEditorGutter::getGutter)
        .orElse(null);
  }
}
