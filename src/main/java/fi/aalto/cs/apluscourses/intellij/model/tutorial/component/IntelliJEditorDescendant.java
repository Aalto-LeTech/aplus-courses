package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.openapi.editor.Editor;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IntelliJEditorDescendant extends TutorialComponent {
  default @NotNull IntelliJEditor getEditorComponent() {
    return Objects.requireNonNull(getAncestorOfType(IntelliJEditor.class),
        "The component should be enclosed by an editor component.");
  }

  default @Nullable Editor getEditor() {
    return getEditorComponent().getEditor();
  }
}
