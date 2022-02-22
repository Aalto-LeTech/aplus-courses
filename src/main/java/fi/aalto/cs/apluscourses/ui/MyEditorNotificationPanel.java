package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.EditorNotificationPanel;
import java.awt.Color;

public class MyEditorNotificationPanel extends EditorNotificationPanel {
  private Color backgroundColor;

  public void setColor(Color color) {
    this.backgroundColor = color;
  }

  @Override
  public Color getBackground() {
    return backgroundColor == null ? super.getBackground() : backgroundColor;
  }
}
