package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import fi.aalto.cs.apluscourses.model.tutorial.CodeRange;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.awt.Rectangle;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJEditorBlock extends IntelliJTutorialComponent<JComponent> implements IntelliJEditorDescendant {
  private final @NotNull CodeRange codeRange;

  public IntelliJEditorBlock(@NotNull CodeRange codeRange,
                             @Nullable TutorialComponent parent,
                             @Nullable Project project) {
    super(parent, project);
    this.codeRange = codeRange;
  }

  @Override
  protected @Nullable Rectangle getBounds(@NotNull JComponent component) {
    var editor = getEditor();
    if (editor == null) {
      return null;
    }
    var start = codeRange.getStartInclusive();
    var end = codeRange.getEndExclusive();
    if (start > end) {
      return null;
    }
    var startPoint = editor.offsetToXY(start, true, false);
    var endPoint = editor.offsetToXY(end, false, true);
    boolean inline = startPoint.y == endPoint.y;
    var rect = component.getVisibleRect().createIntersection(new Rectangle(
        inline ? startPoint.x : 0,
        startPoint.y,
        inline ? endPoint.x - startPoint.x : component.getWidth(),
        endPoint.y - startPoint.y + editor.getLineHeight())).getBounds();
    return SwingUtilities.convertRectangle(component, rect, component.getParent());
  }

  @Override
  protected boolean hasFocusInternal() {
    return super.hasFocusInternal()
        && Optional.ofNullable(getEditor())
        .map(Editor::getCaretModel)
        .map(CaretModel::getOffset)
        .map(this::isInRange)
        .orElse(false);
  }

  @Override
  protected @Nullable JComponent getAwtComponent() {
    return Optional.ofNullable(getEditor()).map(Editor::getContentComponent).orElse(null);
  }

  @Override
  public @NotNull CodeContext getCodeContext() {
    return getEditorComponent().new EditorCodeContext(codeRange);
  }

  @Override
  public @Nullable Document getDocument() {
    return getEditorComponent().getDocument();
  }

  private boolean isInRange(int offset) {
    return codeRange.contains(offset);
  }
}
