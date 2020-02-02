package fi.aalto.cs.intellij.ui.common;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * An abstract {@link MouseListener} that provides default dummy implementations (which actually do
 * nothing) for each method of the interface.
 * Inheriting this class instead of explicitly implementing {@link MouseListener} interface is
 * convenient because the subclass have to override only those methods it wants.
 */
public abstract class AbstractMouseListener implements MouseListener {
  @Override
  public void mouseClicked(MouseEvent mouseEvent) {

  }

  @Override
  public void mousePressed(MouseEvent mouseEvent) {

  }

  @Override
  public void mouseReleased(MouseEvent mouseEvent) {

  }

  @Override
  public void mouseEntered(MouseEvent mouseEvent) {

  }

  @Override
  public void mouseExited(MouseEvent mouseEvent) {

  }
}
