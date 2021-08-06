package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.util.ui.JBUI;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.Border;

public class BalloonShadowBorder implements Border {
  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

  }

  @Override
  public Insets getBorderInsets(Component c) {
    return JBUI.insets(15, 15, 15, 15);
  }

  @Override
  public boolean isBorderOpaque() {
    return false;
  }
}
