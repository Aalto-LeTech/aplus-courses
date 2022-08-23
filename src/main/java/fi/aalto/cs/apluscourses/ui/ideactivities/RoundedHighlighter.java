package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.util.ui.JBValue;
import java.awt.Component;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class RoundedHighlighter extends GenericHighlighter {

  @Override
  public List<RectangularShape> getArea() {
    final float arc = new JBValue.UIInteger("Button.arc", 6).getFloat();
    return Collections.singletonList(
        new RoundRectangle2D.Float(
            0, 0, getComponent().getWidth(), getComponent().getHeight(), arc, arc));
  }

  public RoundedHighlighter(@NotNull Component component) {
    super(component);
  }
}
