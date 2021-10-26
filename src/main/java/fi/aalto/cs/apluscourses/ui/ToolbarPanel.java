package fi.aalto.cs.apluscourses.ui;

import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public interface ToolbarPanel {
  @NotNull JPanel getBasePanel();

  @NotNull JPanel getToolbar();

  @NotNull String getTitle();
}
