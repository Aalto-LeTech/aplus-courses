package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.EditorNotificationPanel;
import java.awt.Color;

public class MyEditorNotificationPanel extends EditorNotificationPanel {
  public void setColor(Color color) {
    this.myBackgroundColor = color;
  }
}
