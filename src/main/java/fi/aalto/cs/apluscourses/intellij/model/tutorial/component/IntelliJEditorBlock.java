package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import fi.aalto.cs.apluscourses.model.tutorial.LineRange;
import java.awt.Component;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJEditorBlock extends IntelliJTutorialComponent<JComponent> {
  private final @NotNull LineRange lineRange;
  private final @NotNull IntelliJEditor editorComponent;

  public IntelliJEditorBlock(@Nullable Path path, @NotNull LineRange lineRange, @Nullable Project project) {
    super(project);
    this.lineRange = lineRange;
    editorComponent = new IntelliJEditor(path, project);
  }

  @Override
  protected @NotNull Rectangle getBounds(@NotNull JComponent component) {
    var editor = editorComponent.getEditor();
    if (editor == null) {
      return new Rectangle();
    }
    var start = editor.logicalPositionToXY(new LogicalPosition(lineRange.getFirst() - 1, 0));
    var end = editor.logicalPositionToXY(new LogicalPosition(lineRange.getLast() - 1, 0));
    Rectangle rect = new Rectangle(
        0,
        start.y,
        component.getWidth(),
        end.y - start.y + editor.getLineHeight());
    return component.getVisibleRect().createIntersection(rect).getBounds();
  }

  @Override
  protected boolean hasFocusInternal() {
    return super.hasFocusInternal()
        && Optional.ofNullable(editorComponent.getEditor())
        .map(Editor::getCaretModel)
        .map(CaretModel::getLogicalPosition)
        .map(this::isInRange)
        .orElse(false);
  }

  @Override
  protected @Nullable JComponent getAwtComponent() {
    return Optional.ofNullable(editorComponent.getEditor()).map(Editor::getContentComponent).orElse(null);
  }

  @Override
  public @NotNull CodeContext getCodeContext() {
    return editorComponent.new EditorCodeContext(lineRange);
  }

  @Override
  @Nullable
  public Document getDocument() {
    return editorComponent.getDocument();
  }

  private boolean isInRange(@NotNull LogicalPosition logicalPosition) {
    int line = logicalPosition.line + 1;
    return lineRange.contains(line);
  }
}
