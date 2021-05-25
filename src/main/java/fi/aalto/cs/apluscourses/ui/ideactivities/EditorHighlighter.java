package fi.aalto.cs.apluscourses.ui.ideactivities;

import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class EditorHighlighter extends GenericHighlighter {
  private final List<Integer> highlightedLines = new ArrayList<>();
  private boolean highlightEverything = false;

  /**
   * Adds particular lines in the editor to the highlight list. The lines are indexed from 1, not 0.
   */
  public void highlightLines(int... lineNumbers) {
    for (int line : lineNumbers) {
      highlightedLines.add(line - 1);
    }
  }

  /**
   * Adds a continuous range of lines in the editor to the highlight list.
   * The lines are indexed from 1, not 0.
   * @param lineBegin The beginning of the intervals of lines, inclusive.
   * @param lineEnd The ending of the intervals of lines, inclusive.
   */
  public void highlightLineRange(int lineBegin, int lineEnd) {
    for (int i = lineBegin; i <= lineEnd; ++i) {
      highlightLines(i);
    }
  }

  public void highlightAllLines() {
    highlightEverything = true;
  }

  @Override
  public @NotNull EditorComponentImpl getComponent() {
    return (EditorComponentImpl) super.getComponent();
  }

  @Override
  public List<Rectangle> getArea() {

    var editor = getComponent().getEditor();
    var lineHeight = editor.getLineHeight();
    var rectangles = new ArrayList<Rectangle>();

    // the parent of the editor is JBViewport, which controls the visible region of the component
    var editorWidth = getComponent().getParent().getWidth();

    if (highlightEverything) {
      return Collections.singletonList(
          new Rectangle(0, 0, editorWidth, getComponent().getHeight()));
    }

    for (int line : highlightedLines) {
      var startPos = new LogicalPosition(line, 0);
      var startPoint = editor.logicalPositionToXY(startPos);
      rectangles.add(new Rectangle(startPoint.x, startPoint.y, editorWidth, lineHeight));
    }

    return rectangles;
  }

  public EditorHighlighter(@NotNull EditorComponentImpl component) {
    super(component);
  }
}
