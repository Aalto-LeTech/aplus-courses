package fi.aalto.cs.intellij.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import org.jetbrains.annotations.NotNull;

public class RelayAction extends AbstractAction {
  private final ActionListener actionListener;

  public RelayAction(@NotNull ActionListener actionListener) {
    this.actionListener = actionListener;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    actionListener.actionPerformed(actionEvent);
  }
}
