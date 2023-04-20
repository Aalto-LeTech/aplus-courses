package fi.aalto.cs.apluscourses.ui.tutorials;

import java.awt.Component;
import java.awt.Point;
import java.awt.geom.Area;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PieceSystem implements Piece {

  private static final AreaCalculationStrategy ADDITIVE = new AdditiveStrategy();
  private static final AreaCalculationStrategy SUBTRACTIVE = new SubtractiveStrategy();

  private @NotNull Set<@NotNull Piece> pieces = new HashSet<>();

    @Override
  public @NotNull Area getArea(@NotNull Component destination) {
    return getArea(destination, false);
  }

  public @NotNull Area getArea(@NotNull Component destination, boolean complement) {
    var strategy = complement ? SUBTRACTIVE : ADDITIVE;
    var area = strategy.getInitialArea(destination);
    for (var piece : pieces) {
      strategy.apply(area, piece.getArea(destination));
    }
    return area;
  }

  public boolean contains(@Nullable Point point) {
    return pieces.stream().anyMatch(piece -> piece.contains(point));
  }

  public void addPiece(@NotNull Piece piece) {
    pieces.add(piece);
  }

  public void removePiece(Piece piece) {
    pieces.remove(piece);
  }

  public boolean isEmpty() {
    return pieces.isEmpty();
  }

  public boolean hasFocus() {
    return pieces.stream().anyMatch(Piece::hasFocus);
  }

  private interface AreaCalculationStrategy {
    @NotNull Area getInitialArea(@NotNull Component component);

    void apply(@NotNull Area lhs, @NotNull Area rhs);
  }

  private static class AdditiveStrategy implements AreaCalculationStrategy {
    @Override
    public @NotNull Area getInitialArea(@NotNull Component destination) {
      return new Area();
    }

    @Override
    public void apply(@NotNull Area rhs, @NotNull Area lhs) {
      rhs.add(lhs);
    }
  }

  private static class SubtractiveStrategy implements AreaCalculationStrategy {
    @Override
    public @NotNull Area getInitialArea(@NotNull Component destination) {
      return new Area(destination.getBounds());
    }

    @Override
    public void apply(@NotNull Area lhs, @NotNull Area rhs) {
      lhs.subtract(rhs);
    }
  }
}
