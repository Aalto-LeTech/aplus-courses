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
  protected @NotNull Rectangle getBounds(@NotNull JComponent component) {
    var editor = getEditor();
    if (editor == null) {
      return new Rectangle();
    }
    var start = editor.logicalPositionToXY(new LogicalPosition(lineRange.getFirst() - 1, 0));
    var end = editor.logicalPositionToXY(new LogicalPosition(lineRange.getLast() - 1, 0));
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
  @Nullable
  public Document getDocument() {
    return getEditorComponent().getDocument();
  }

  private boolean isInRange(@NotNull LogicalPosition logicalPosition) {
    int line = logicalPosition.line + 1;
    return lineRange.contains(line);
  }
}
