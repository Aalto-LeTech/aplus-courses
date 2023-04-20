package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.Component;
import java.awt.Graphics;
import org.jetbrains.annotations.NotNull;

public class Highlighting {
  private final @NotNull Component component;
  private final boolean paintBackgroundWhenPassive;
  private final @NotNull PieceSystem pieceSystem;
  private final @NotNull AreaPainter activePainter;
  private final @NotNull AreaPainter passivePainter;

  public Highlighting(@NotNull Component component,
                      @NotNull AreaPainter activePainter,
                      @NotNull AreaPainter passivePainter,
                      boolean paintBackgroundWhenPassive) {
    this.component = component;
    this.paintBackgroundWhenPassive = paintBackgroundWhenPassive;
    this.pieceSystem = new PieceSystem();
    this.activePainter = activePainter;
    this.passivePainter = passivePainter;
  }

  public void addPiece(@NotNull Piece piece) {
    pieceSystem.addPiece(piece);
  }

  public void removePiece(@NotNull Piece piece) {
    pieceSystem.removePiece(piece);
  }

  public boolean paint(Graphics graphics) {
    if (pieceSystem.isEmpty()) {
      return false;
    }
    boolean retVal;
    if (pieceSystem.isActive()) {
      activePainter.opacity().set(0f);
      activePainter.patternY().set(0f);
      passivePainter.opacity().fadeIn();
      retVal = false;
    } else {
      activePainter.opacity().animate(0.3f, 1f, 2000, AnimatedValue.BACK_AND_FORTH);
      activePainter.patternY().animate(0f, 1f, 2000, AnimatedValue.START_OVER);
      passivePainter.opacity().fadeOut();
      retVal = true;
    }
    activePainter.paint(graphics, pieceSystem.getArea(component, false));
    passivePainter.paint(graphics, pieceSystem.getArea(component, paintBackgroundWhenPassive));
    return retVal;
  }
}
