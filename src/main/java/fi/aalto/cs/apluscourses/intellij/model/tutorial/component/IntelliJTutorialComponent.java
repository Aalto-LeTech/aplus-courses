package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.ui.tutorials.SupportedPiece;
import fi.aalto.cs.apluscourses.utils.GeometryUtil;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IntelliJTutorialComponent<C extends Component> implements TutorialComponent, SupportedPiece {
  private static final int MARGIN = 15;
  private final @Nullable TutorialComponent parent;
  private final @Nullable Project project;
  private final @NotNull Set<@NotNull Object> supporters = new HashSet<>();

  protected IntelliJTutorialComponent(@Nullable TutorialComponent parent, @Nullable Project project) {
    this.parent = parent;
    this.project = project;
  }

  protected abstract @Nullable C getAwtComponent();

  @Nullable
  public Document getDocument() {
    return null;
  }

  protected @NotNull Rectangle getBounds(@NotNull C component) {
    return component.getBounds();
  }

  public @Nullable Project getProject() {
    return project;
  }

  @Override
  public @NotNull Area getArea(@NotNull Component destination) {
    var component = getAwtComponent();
    if (component == null) {
      return new Area();
    }
    var area = new Area(GeometryUtil.withMargin(getBounds(component), MARGIN));
    var offset = SwingUtilities.convertPoint(component.getParent(), 0, 0, destination);
    area.transform(AffineTransform.getTranslateInstance(offset.x, offset.y));
    return area;
  }

  @Override
  public boolean hasFocus() {
    if (hasSupporters()) {
      return true;
    }
    return hasFocusInternal();
  }

  protected boolean hasFocusInternal() {
    var focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    return focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, getAwtComponent());
  }

  @Override
  public boolean contains(@Nullable Point point) {
    if (point == null) {
      return false;
    }
    var component = getAwtComponent();
    if (component == null) {
      return false;
    }
    var newPoint = new Point(point);
    SwingUtilities.convertPointFromScreen(newPoint, component);
    return getBounds(component).contains(newPoint);
  }

  @Override
  public void addSupporter(@NotNull Object supporter) {
    supporters.add(supporter);
  }

  @Override
  public void removeSupporter(@NotNull Object supporter) {
    supporters.remove(supporter);
  }

  @Override
  public boolean hasSupporters() {
    return !supporters.isEmpty();
  }

  @Override
  public @Nullable TutorialComponent getParent() {
    return parent;
  }
}
