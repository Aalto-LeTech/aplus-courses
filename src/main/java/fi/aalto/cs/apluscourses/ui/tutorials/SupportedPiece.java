package fi.aalto.cs.apluscourses.ui.tutorials;

import org.jetbrains.annotations.NotNull;

public interface SupportedPiece extends Piece {
  void addSupporter(@NotNull Object object);

  void removeSupporter(@NotNull Object object);

  boolean hasSupporters();
}
