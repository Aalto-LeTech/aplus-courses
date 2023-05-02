package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import fi.aalto.cs.apluscourses.model.tutorial.LineRange;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.awt.Rectangle;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJEditorBlock extends IntelliJTutorialComponent<JComponent> implements IntelliJEditorDescendant {
  private final @NotNull LineRange lineRange;

  public IntelliJEditorBlock(@NotNull LineRange lineRange,
                             @Nullable TutorialComponent parent,
                             @Nullable Project project) {
    super(parent, project);
    this.lineRange = lineRange;
  }

  @Override
  protected @Nullable Rectangle getBounds(@NotNull JComponent component) {
    var editor = getEditor();
    if (editor == null) {
      return null;
    }
    var first = lineRange.getFirst() - 1;
    var last = lineRange.getLast() - 1;
    if (first > last) {
      return null;
    }
    var start = editor.logicalPositionToXY(new LogicalPosition(first, 0));
    var end = editor.logicalPositionToXY(new LogicalPosition(last, 0));
    var rect = component.getVisibleRect().createIntersection(new Rectangle(
        0,
        start.y,
        component.getWidth(),
        end.y - start.y + editor.getLineHeight())).getBounds();
    return SwingUtilities.convertRectangle(component, rect, component.getParent());
  }

  @Override
  protected boolean hasFocusInternal() {
    return super.hasFocusInternal()
        && Optional.ofNullable(getEditor())
        .map(Editor::getCaretModel)
        .map(CaretModel::getLogicalPosition)
        .map(this::isInRange)
        .orElse(false);
  }

  @Override
  protected @Nullable JComponent getAwtComponent() {
    return Optional.ofNullable(getEditor()).map(Editor::getContentComponent).orElse(null);
  }

  @Override
  public @NotNull CodeContext getCodeContext() {
    return getEditorComponent().new EditorCodeContext(lineRange);
  }

  @Override
  public @Nullable Document getDocument() {
    return getEditorComponent().getDocument();
  }

  private boolean isInRange(@NotNull LogicalPosition logicalPosition) {
    int line = logicalPosition.line + 1;
    return lineRange.contains(line);
  }
}
